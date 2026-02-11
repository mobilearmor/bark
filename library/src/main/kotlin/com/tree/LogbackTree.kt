package com.tree

import android.util.Log
import org.slf4j.LoggerFactory
import timber.log.Timber


class LogbackTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Get logger for the tag (or use default if no tag)
        val logger = LoggerFactory.getLogger(if (tag != null) tag else "App")

        // Route to appropriate SLF4J log level
        when (priority) {
            Log.VERBOSE, Log.DEBUG -> if (t != null) {
                logger.debug(message, t)
            } else {
                logger.debug(message)
            }

            Log.INFO -> if (t != null) {
                logger.info(message, t)
            } else {
                logger.info(message)
            }

            Log.WARN -> if (t != null) {
                logger.warn(message, t)
            } else {
                logger.warn(message)
            }

            Log.ERROR, Log.ASSERT -> if (t != null) {
                logger.error(message, t)
            } else {
                logger.error(message)
            }
        }
    }
}