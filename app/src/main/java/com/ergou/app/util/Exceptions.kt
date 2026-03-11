package com.ergou.app.util

/**
 * 二狗自定义异常体系
 */
open class ErgouException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * API/网络相关异常
 */
class ApiException(
    message: String,
    cause: Throwable? = null,
    val statusCode: Int? = null
) : ErgouException(message, cause)

/**
 * 工具执行异常
 */
class ToolException(
    val toolName: String,
    message: String,
    cause: Throwable? = null
) : ErgouException("[$toolName] $message", cause)

/**
 * 数据库/DataStore 存储异常
 */
class StorageException(
    message: String,
    cause: Throwable? = null
) : ErgouException(message, cause)
