package de.vinz.openfls.domains.clients.archive.export

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.springframework.stereotype.Service

@Service
class ClientArchiveExportStorage {

    private val exportDirectory: Path = Paths.get(
        System.getProperty("java.io.tmpdir"),
        "openfls-client-archive-exports"
    )

    fun writeExport(clientId: Long, fileName: String, content: ByteArray): Path {
        val exportFile = resolveExportFilePath(clientId, fileName)
        Files.createDirectories(exportFile.parent)
        Files.write(exportFile, content)
        return exportFile
    }

    fun exists(clientId: Long, fileName: String): Boolean {
        return Files.exists(resolveExportFilePath(clientId, fileName))
    }

    fun read(clientId: Long, fileName: String): ByteArray {
        return Files.readAllBytes(resolveExportFilePath(clientId, fileName))
    }

    fun delete(clientId: Long, fileName: String) {
        Files.deleteIfExists(resolveExportFilePath(clientId, fileName))
    }

    fun delete(filePath: String) {
        Files.deleteIfExists(Path.of(filePath))
    }

    fun exists(filePath: String): Boolean {
        return Files.exists(Path.of(filePath))
    }

    fun read(filePath: String): ByteArray {
        return Files.readAllBytes(Path.of(filePath))
    }

    private fun resolveExportFilePath(clientId: Long, fileName: String): Path {
        return Paths.get(exportDirectory.toString(), clientId.toString(), fileName)
    }
}
