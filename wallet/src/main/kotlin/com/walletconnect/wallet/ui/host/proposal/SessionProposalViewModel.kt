package com.walletconnect.wallet.ui.host.proposal

import android.util.Log
import androidx.lifecycle.ViewModel
import com.walletconnect.sample_common.EthChains
import com.walletconnect.sample_common.tag
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.domain.mapOfAllAccounts
import com.walletconnect.walletconnectv2.client.Sign
import com.walletconnect.walletconnectv2.client.SignClient

class SessionProposalViewModel : ViewModel() {

    fun fetchSessionProposal(sessionExists: (SessionProposalUI) -> Unit, sessionDNE: () -> Unit) {
        if (WalletDelegate.sessionProposal != null) {
            val sessionProposalUI = generateSessionProposalEvent(WalletDelegate.sessionProposal!!)
            sessionExists(sessionProposalUI)
        } else {
            sessionDNE()
        }
    }

    fun approve() {
        if (WalletDelegate.sessionProposal != null && WalletDelegate.selectedChainAddressId in mapOfAllAccounts.keys) {
            val selectedAccounts: Map<EthChains, String> = mapOfAllAccounts[WalletDelegate.selectedChainAddressId] ?: throw Exception("Can't find account")
            val sessionProposal: Sign.Model.SessionProposal = requireNotNull(WalletDelegate.sessionProposal)
            val sessionNamespaces: Map<String, Sign.Model.Namespace.Session> = selectedAccounts.filter { (chain: EthChains, _) ->
                "${chain.chainNamespace}:${chain.chainReference}" in sessionProposal.requiredNamespaces.values.flatMap { it.chains }
            }.toList().groupBy { (chain: EthChains, _: String) ->
                chain.chainNamespace
            }.map { (namespaceKey: String, chainData: List<Pair<EthChains, String>>) ->
                val accounts = chainData.map { (chain: EthChains, accountAddress: String) ->
                    "${chain.chainNamespace}:${chain.chainReference}:${accountAddress}"
                }
                val methods = sessionProposal.requiredNamespaces.values.flatMap { it.methods }
                val events = sessionProposal.requiredNamespaces.values.flatMap { it.events }

                namespaceKey to Sign.Model.Namespace.Session(accounts = accounts, methods = methods, events = events, extensions = null)
            }.toMap()

            val approveProposal = Sign.Params.Approve(
                proposerPublicKey = sessionProposal.proposerPublicKey,
                namespaces = sessionNamespaces
            )

            SignClient.approveSession(approveProposal) { error ->
                Log.e(tag(this@SessionProposalViewModel), error.throwable.stackTraceToString())
            }

            WalletDelegate.clearCache()
        }
    }

    fun reject() {
        WalletDelegate.sessionProposal?.let { sessionProposal ->
            val rejectionReason = "Reject Session"
            val reject = Sign.Params.Reject(
                proposerPublicKey = sessionProposal.proposerPublicKey,
                reason = rejectionReason,
                code = 406
            )

            SignClient.rejectSession(reject) { error ->
                Log.d(tag(this@SessionProposalViewModel), "sending reject error: $error")
            }

            WalletDelegate.clearCache()
        }
    }

    private fun generateSessionProposalEvent(sessionProposal: Sign.Model.SessionProposal): SessionProposalUI {
        return SessionProposalUI(
            peerIcon = sessionProposal.icons.first().toString(),
            peerName = sessionProposal.name,
            proposalUri = sessionProposal.url,
            peerDescription = sessionProposal.description,
            chains = sessionProposal.requiredNamespaces.flatMap { it.value.chains }.joinToString("\n"),
            methods = sessionProposal.requiredNamespaces.flatMap { it.value.methods }.joinToString("\n"),
            events = sessionProposal.requiredNamespaces.flatMap { it.value.events }.joinToString("\n")
        )
    }
}