package com.yano.interplan.imble3_app

class Misc {
    companion object {
        @JvmStatic
        fun s2b(s: String?): ByteArray {
            if (s.isNullOrEmpty()) return byteArrayOf()

            val result = mutableListOf<Byte>()
            val hexChars = "0123456789abcdef"

            var i = 0
            while (i < s.length) {
                // スペースやカンマはスキップ
                if (s[i] == ' ' || s[i] == ',') {
                    i++
                    continue
                }

                // 2文字をチェックして16進数か確認
                if (i + 1 >= s.length ||
                    s[i].lowercaseChar() !in hexChars ||
                    s[i + 1].lowercaseChar() !in hexChars
                ) {
                    break
                }

                // 2文字をバイトに変換して追加
                val byte = s.substring(i, i + 2).toInt(16)
                result.add(byte.toByte())
                i += 2
            }

            return result.toByteArray()
        }
    }
}