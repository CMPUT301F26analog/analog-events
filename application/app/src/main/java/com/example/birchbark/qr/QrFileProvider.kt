package com.example.birchbark.qr

import androidx.core.content.FileProvider

/**
 * US 02.02.01
 * FileProvider subclass for read-only sharing of temporary
 * QR PNGs. Its scope is restricted by res/xml/qr_file_paths.xml and the manifest.
 */
class QrFileProvider : FileProvider()