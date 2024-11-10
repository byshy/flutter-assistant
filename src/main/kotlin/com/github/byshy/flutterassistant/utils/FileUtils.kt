package com.github.byshy.flutterassistant.utils

class FileUtils {
    companion object {
        fun getEventBLoCFile(eventFileName: String) : String {
            val fileNameChunks = getFileNameChunks(eventFileName)

            val blocFileName = fileNameChunks.joinToString("_") + "_bloc"
            val blocFileNameWithExtension = "$blocFileName.dart"

            return blocFileNameWithExtension
        }

        fun isEventFile(eventFileName: String, eventFileContent: String) : Boolean {
            val fileNameChunks = getFullFileNameChunks(eventFileName)
            val eventBLoCFileName = getEventBLoCFile(eventFileName)

            val isPartOfBloc = eventFileContent.startsWith("part of '$eventBLoCFileName';")
            val isEventsFile = fileNameChunks.last() == "event"

            return isEventsFile && isPartOfBloc
        }

        private fun getFullFileNameChunks(fileNameWithExtension: String) : List<String> {
            val fileName = fileNameWithExtension.removeSuffix(".dart")
            val fileNameChunks = fileName.split("_")

            return fileNameChunks
        }

        private fun getFileNameChunks(fileNameWithExtension: String) : List<String> {
            val fileName = fileNameWithExtension.removeSuffix(".dart")
            val fileNameChunks = fileName.split("_")

            return fileNameChunks.dropLast(1)
        }
    }
}