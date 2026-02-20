package com.swappy.aicalcount.util

import androidx.annotation.StringRes
import com.swappy.aicalcount.R

/**
 * Sealed class for app errors with user-facing messages and optional retry.
 */
sealed class AppError(
    @StringRes open val messageRes: Int,
    open val retryable: Boolean = false,
    open val detail: String? = null
) {
    data class Network(
        override val detail: String? = null
    ) : AppError(
        messageRes = R.string.error_network,
        retryable = true,
        detail = detail
    )

    data class ApiLimit(
        override val detail: String? = null
    ) : AppError(
        messageRes = R.string.error_api_limit,
        retryable = false,
        detail = detail
    )

    data class Server(
        override val detail: String? = null
    ) : AppError(
        messageRes = R.string.error_server,
        retryable = true,
        detail = detail
    )

    data class InvalidInput(
        @StringRes override val messageRes: Int = R.string.error_invalid_input,
        override val detail: String? = null
    ) : AppError(messageRes = messageRes, retryable = false, detail = detail)

    data class Unknown(
        override val detail: String? = null
    ) : AppError(
        messageRes = R.string.error_unknown,
        retryable = true,
        detail = detail
    )
}
