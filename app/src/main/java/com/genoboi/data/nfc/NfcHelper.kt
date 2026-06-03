package com.genoboi.data.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log

object NfcHelper {

    // ── URL base do site público dos animais ──────────────────────────────────
    // Altere para o domínio onde o genia_site está publicado (Vercel/etc.)
    const val SITE_BASE_URL = "https://geniasite.vercel.app"

    fun buildAnimalUrl(supabaseId: String): String = "$SITE_BASE_URL/a/$supabaseId"

    // ── Escreve URL na tag NFC ─────────────────────────────────────────────────
    // Retorna true em sucesso, false em falha.
    fun escreverUrl(tag: Tag, url: String): Boolean {
        val mensagem = NdefMessage(arrayOf(NdefRecord.createUri(url)))

        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    Log.w("NFC", "Tag somente leitura")
                    ndef.close()
                    return false
                }
                if (ndef.maxSize < mensagem.toByteArray().size) {
                    Log.w("NFC", "Tag sem espaço suficiente")
                    ndef.close()
                    return false
                }
                ndef.writeNdefMessage(mensagem)
                ndef.close()
                Log.d("NFC", "URL gravada: $url")
                true
            } else {
                // Tag ainda não formatada — tenta formatar e gravar
                val formatable = NdefFormatable.get(tag)
                if (formatable != null) {
                    formatable.connect()
                    formatable.format(mensagem)
                    formatable.close()
                    Log.d("NFC", "Tag formatada e URL gravada: $url")
                    true
                } else {
                    Log.w("NFC", "Tag não suporta NDEF")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("NFC", "Erro ao gravar tag: ${e.message}")
            false
        }
    }
}
