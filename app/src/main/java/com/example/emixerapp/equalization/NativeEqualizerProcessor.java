package com.example.emixerapp.equalization;

import android.util.Log;

public class NativeEqualizerProcessor {

    private static final String TAG = "NativeEqualizerProcessor";

    // Carrega a biblioteca nativa 'emixer' que contém a implementação C++
    static {
        try {
            System.loadLibrary("emixer");
            Log.d(TAG, "Native library 'emixer' loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library 'emixer': " + e.getMessage());
        }
    }

    /**
     * Declaração do método nativo C++ para aplicar equalização.
     * Este método receberá um array de dados de áudio (short[]) e um array de ganhos (int[]),
     * e aplicará os ganhos diretamente aos dados de áudio no código C++.
     *
     * @param audioData Um array de shorts representando as amostras de áudio. Será modificado.
     * @param gains Um array de inteiros representando os ganhos para as bandas de equalização.
     *              No exemplo C++, o primeiro elemento deste array é usado como fator de ganho.
     * @return O número de amostras processadas, ou -1 em caso de erro.
     */
    public native int applyEqualizationNative(short[] audioData, int[] gains);
}
