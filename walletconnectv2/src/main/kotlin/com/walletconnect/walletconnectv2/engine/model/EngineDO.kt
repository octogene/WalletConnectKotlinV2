package com.walletconnect.walletconnectv2.engine.model

import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.type.EngineEvent
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import java.net.URI

internal sealed class EngineDO {

    internal sealed class ProposedSequence {
        class Pairing(val uri: String) : ProposedSequence()
        object Session : ProposedSequence()
    }

    internal class WalletConnectUri(
        val topic: TopicVO,
        val symKey: SecretKey,
        val relay: RelayProtocolOptionsVO,
        val version: String = "2"
    ) : EngineDO()

    internal data class SessionProposal(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<URI>,
        val requiredNamespaces: Map<String, Namespace.Proposal>,
        val proposerPublicKey: String,
        val relayProtocol: String,
        val relayData: String?,
    ) : EngineDO(), EngineEvent

    internal sealed class Namespace: EngineDO() {

        data class Proposal(
            val chains: List<String>,
            val methods: List<String>,
            val events: List<String>,
            val extensions: List<Extension>?
        ): Namespace() {

            data class Extension(val chains: List<String>, val methods: List<String>, val events: List<String>)
        }

        data class Session(
            val accounts: List<String>,
            val methods: List<String>,
            val events: List<String>,
            val extensions: List<Extension>?
        ): Namespace() {

            data class Extension(val accounts: List<String>, val methods: List<String>, val events: List<String>)
        }
    }

    internal data class RelayProtocolOptions(val protocol: String, val data: String? = null) : EngineDO()

    internal data class SessionRequest(
        val topic: String,
        val chainId: String?,
        val peerAppMetaData: AppMetaData?,
        val request: JSONRPCRequest,
    ) : EngineDO(), EngineEvent {

        internal data class JSONRPCRequest(
            val id: Long,
            val method: String,
            val params: String,
        ) : EngineDO()
    }

    internal data class SessionPayloadResponse(
        val topic: String,
        val chainId: String?,
        val method: String,
        val result: JsonRpcResponse,
    ) : EngineDO(), EngineEvent

    internal data class SessionDelete(
        val topic: String,
        val reason: String,
    ) : EngineDO(), EngineEvent

    internal data class DeletedPairing(
        val topic: String,
        val reason: String,
    ) : EngineDO(), EngineEvent

    internal data class SessionEvent(
        val topic: String,
        val name: String,
        val data: String,
        val chainId: String?,
    ) : EngineDO(), EngineEvent

   internal sealed class SettledSessionResponse : EngineDO(), EngineEvent {
        data class Result(val settledSession: Session) : SettledSessionResponse()
        data class Error(val errorMessage: String) : SettledSessionResponse()
    }

    //todo: remove
    internal sealed class SessionUpdateAccountsResponse : EngineDO(), EngineEvent {
        data class Result(val topic: TopicVO, val accounts: List<String>) : SessionUpdateAccountsResponse()
        data class Error(val errorMessage: String) : SessionUpdateAccountsResponse()
    }

    internal sealed class SessionUpdateNamespacesResponse : EngineDO(), EngineEvent {
        data class Result(val topic: TopicVO, val namespaces: Map<String, Namespace.Session>) : SessionUpdateNamespacesResponse()
        data class Error(val errorMessage: String) : SessionUpdateNamespacesResponse()
    }

    internal data class SessionRejected(
        val topic: String,
        val reason: String,
    ) : EngineDO(), EngineEvent

    internal data class SessionApproved(
        val topic: String,
        val peerAppMetaData: AppMetaData?,
        val accounts: List<String>,
        val namespaces: Map<String, Namespace.Session>,
    ) : EngineDO(), EngineEvent

    internal data class PairingSettle(val topic: TopicVO, val metaData: AppMetaData?) : EngineDO(), EngineEvent
    internal data class SessionUpdateAccounts(val topic: TopicVO, val accounts: List<String>) : EngineDO(), EngineEvent
    internal data class SessionUpdateNamespaces(val topic: TopicVO, val namespaces: Map<String, Namespace.Session>) : EngineDO(), EngineEvent

    internal data class SessionExtend(
        override val topic: TopicVO,
        override val expiry: ExpiryVO,
        val namespaces: Map<String, Namespace.Session>,
        val peerAppMetaData: AppMetaData?,
    ) : EngineDO(), Sequence, EngineEvent

    internal data class Session(
        override val topic: TopicVO,
        override val expiry: ExpiryVO,
        val namespaces: Map<String, Namespace.Session>,
        val peerAppMetaData: AppMetaData?,
    ) : EngineDO(), Sequence, EngineEvent

    internal data class Event(
        val name: String,
        val data: String,
        val chainId: String,
    ) : EngineDO()

    internal data class AppMetaData(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<String>,
    ) : EngineDO()

    internal data class ConnectionState(
        val isAvailable: Boolean
    ) : EngineDO(), EngineEvent

    internal sealed class JsonRpcResponse : EngineDO() {
        abstract val id: Long

        @JsonClass(generateAdapter = true)
        internal data class JsonRpcResult(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val result: String,
        ) : JsonRpcResponse()

        @JsonClass(generateAdapter = true)
        internal data class JsonRpcError(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val error: Error,
        ) : JsonRpcResponse()

        internal data class Error(
            val code: Int,
            val message: String,
        )
    }

    internal data class Request(
        val topic: String,
        val method: String,
        val params: String,
        val chainId: String,
    ) : EngineDO()
}