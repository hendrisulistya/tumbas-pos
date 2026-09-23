package com.argminres.app.util

object CsvParser {
    fun parseCsvLine(line: String): List<String> {
        val sanitizedLine = line.replace("\uFEFF", "").trim()
        if (sanitizedLine.isEmpty()) return emptyList()
        
        val tokens = mutableListOf<String>()
        var start = 0
        var inQuotes = false
        for (i in sanitizedLine.indices) {
            if (sanitizedLine[i] == '\"') {
                inQuotes = !inQuotes
            } else if (sanitizedLine[i] == ',' && !inQuotes) {
                tokens.add(sanitizedLine.substring(start, i).trim().removeSurrounding("\""))
                start = i + 1
            }
        }
        tokens.add(sanitizedLine.substring(start).trim().removeSurrounding("\""))
        return tokens
    }
}
