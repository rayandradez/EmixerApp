// equalizer_processor.cpp

#include <jni.h>
#include <android/log.h>
#include <cmath> // Para funções matemáticas como pow(), sin(), cos()
#include <limits.h> // Para SHRT_MAX e SHRT_MIN

// Definições de log para o Logcat
#ifndef ALOGD
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "NativeEqualizer", __VA_ARGS__)
#endif
#ifndef ALOGI
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, "NativeEqualizer", __VA_ARGS__)
#endif
#ifndef ALOGW
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, "NativeEqualizer", __VA_ARGS__)
#endif
#ifndef ALOGE
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, "NativeEqualizer", __VA_ARGS__)
#endif

// --- Enumeração para tipos de filtro biquad ---
enum FilterType {
    LOW_SHELF,      // Para graves
    PEAKING,        // Para médios
    HIGH_SHELF,     // Para agudos
    FLAT_GAIN       // Apenas ganho sem filtragem de frequência
};

// --- Classe para um filtro biquad ---
class BiquadFilter {
public:
    // Coeficientes do filtro
    double b0, b1, b2, a1, a2;
    // Variáveis de estado (memória do filtro)
    double v1, v2;

    BiquadFilter() : b0(1.0), b1(0.0), b2(0.0), a1(0.0), a2(0.0), v1(0.0), v2(0.0) {}

    // Processa uma única amostra de áudio
    double process(double input) {
        double output = b0 * input + b1 * v1 + b2 * v2 - a1 * v1 - a2 * v2;
        v2 = v1;
        v1 = input; // Para a forma direta 1
        return output;
    }

    // Reseta as variáveis de estado do filtro
    void reset() {
        v1 = 0.0;
        v2 = 0.0;
    }

    // Configura o filtro biquad com base no tipo, ganho, frequência, Q e taxa de amostragem
    void setup(FilterType type, double gainDb, double freqHz, double Q, double sampleRate) {
        reset(); // Reseta o estado ao configurar

        double A = pow(10, gainDb / 40.0); // Ganho linear (para shelf e peaking)
        double omega = 2 * M_PI * freqHz / sampleRate;
        double sin_omega = sin(omega);
        double cos_omega = cos(omega);
        double alpha = sin_omega / (2 * Q); // Q-factor para peaking
        double beta = sqrt(A) / Q; // Q-factor para shelf

        b0 = 0; b1 = 0; b2 = 0; a1 = 0; a2 = 0;

        switch (type) {
            case FLAT_GAIN:
                b0 = pow(10, gainDb / 20.0); // Apenas aplica o ganho geral
                b1 = b2 = a1 = a2 = 0;
                break;
            case LOW_SHELF: // Filtro low-shelf (graves)
                b0 = A * ((A + 1) - (A - 1) * cos_omega + beta * sin_omega);
                b1 = 2 * A * ((A - 1) - (A + 1) * cos_omega);
                b2 = A * ((A + 1) - (A - 1) * cos_omega - beta * sin_omega);
                a1 = -2 * ((A - 1) + (A + 1) * cos_omega);
                a2 = (A + 1) + (A - 1) * cos_omega - beta * sin_omega;
                break;
            case HIGH_SHELF: // Filtro high-shelf (agudos)
                b0 = A * ((A + 1) + (A - 1) * cos_omega + beta * sin_omega);
                b1 = -2 * A * ((A - 1) + (A + 1) * cos_omega);
                b2 = A * ((A + 1) + (A - 1) * cos_omega - beta * sin_omega);
                a1 = -2 * ((A - 1) - (A + 1) * cos_omega);
                a2 = (A + 1) - (A - 1) * cos_omega - beta * sin_omega;
                break;
            case PEAKING: // Filtro peaking (médios)
                b0 = 1 + alpha * A;
                b1 = -2 * cos_omega;
                b2 = 1 - alpha * A;
                a1 = -2 * cos_omega;
                a2 = 1 - alpha / A;
                break;
        }

        // Normalização: divide todos os coeficientes por a0 (que é o 'a2' no cálculo acima para shelf, e 1+alpha/A para peaking)
        // Para peaking, o a0 é (1 + alpha / A)
        // Para shelf, o a0 é (A + 1) + (A - 1) * cos_omega + beta * sin_omega (low-shelf)
        // ou (A + 1) - (A - 1) * cos_omega + beta * sin_omega (high-shelf)
        // Onde 'a2' é o termo do denominador quando o filtro é implementado na forma canônica
        // No caso de peaking, o a0 é (1 + alpha / A).
        // Nosso a2 já é o coeficiente do denominador, então ajustamos o a1 e a2.
        // A forma como os coeficientes são calculados depende da "topologia" do biquad.
        // As fórmulas acima são para uma forma específica.
        // Para as fórmulas de cookbook mais comuns, o a0 é geralmente 1 ou um fator de normalização.
        // Vamos usar uma normalização mais genérica aqui, dividindo por a2_final
        double a0_norm = 1.0; // Assume a0 = 1 para a forma direta 1
        if (type == LOW_SHELF || type == HIGH_SHELF) {
            a0_norm = (A + 1) + (A - 1) * cos_omega + beta * sin_omega; // Denominador para low-shelf
            if (type == HIGH_SHELF) {
                a0_norm = (A + 1) - (A - 1) * cos_omega + beta * sin_omega; // Denominador para high-shelf
            }
        } else if (type == PEAKING) {
            a0_norm = (1 + alpha / A); // Denominador para peaking
        }

        // Re-normalize os coeficientes 'a' e 'b'
        b0 /= a0_norm;
        b1 /= a0_norm;
        b2 /= a0_norm;
        a1 /= a0_norm;
        a2 /= a0_norm;


        // As fórmulas de cookbook para biquads são complexas e podem variar ligeiramente.
        // As implementações são sensíveis à precisão.
        // Para este exemplo, estamos usando uma versão simplificada para ilustrar o conceito.
        // Para um equalizador de produção, você usaria bibliotecas DSP mais robustas.
    }
};


extern "C" JNIEXPORT jint JNICALL
Java_com_example_emixerapp_equalization_NativeEqualizerProcessor_applyEqualizationNative(JNIEnv *env,
                                                                                         jobject /* this */,
                                                                                         jshortArray audioData,
                                                                                         jintArray gains){
    ALOGD("NativeEqualizer: applyEqualizationNative chamado.");

    jshort* audioDataPtr = env->GetShortArrayElements(audioData, 0);
    jint* gainsPtr = env->GetIntArrayElements(gains, 0);

    if (!audioDataPtr || !gainsPtr) {
        ALOGE("NativeEqualizer: Falha ao obter ponteiros para arrays de dados.");
        if (audioDataPtr) env->ReleaseShortArrayElements(audioData, audioDataPtr, JNI_ABORT);
        if (gainsPtr) env->ReleaseIntArrayElements(gains, gainsPtr, JNI_ABORT);
        return -1;
    }

    int numSamples = env->GetArrayLength(audioData);
    int numGains = env->GetArrayLength(gains);

    if (numGains < 3) { // Esperamos pelo menos 3 ganhos: Bass, Mid, High
        ALOGE("NativeEqualizer: Array de ganhos inválido. Esperado 3, recebido %d.", numGains);
        env->ReleaseShortArrayElements(audioData, audioDataPtr, JNI_ABORT);
        env->ReleaseIntArrayElements(gains, gainsPtr, JNI_ABORT);
        return -1;
    }

    // Ganhos em dB. Os valores passados do Java (ex: 1000, 2000) precisam ser mapeados.
    // Vamos assumir que 1000 = 0dB (flat), 2000 = +6dB, 500 = -6dB.
    // O mapeamento exato do slider para dB pode ser feito no Java ou aqui.
    // Por enquanto, assumimos que os ganhos são passados como 1000 = 0dB.
    // Para simplificar, vamos interpretar `gainsPtr[X]` como um valor de 0 a 100
    // que mapeamos para -15dB a +15dB, ou o que for mais fácil para o teste.
    // Para a demo, vamos usar os valores `1000, 2000, 500` diretamente como multiplicadores,
    // e o `setup` do filtro biquad converterá para dB.

    // Frequências centrais e Q-factors para os filtros (exemplo - ajuste conforme necessário)
    const double SAMPLE_RATE = 44100.0;
    const double BASS_FREQ = 100.0;   // Frequência de corte para graves
    const double MID_FREQ = 1000.0;   // Frequência central para médios
    const double HIGH_FREQ = 5000.0;  // Frequência de corte para agudos
    const double Q_FACTOR = 0.707;    // Q-factor típico para filtros de áudio (Butterworth)

    // Instâncias dos filtros
    BiquadFilter bassFilter;
    BiquadFilter midFilter;
    BiquadFilter highFilter;

    // Converte o ganho linear (ex: 1000, 2000) para dB.
    // Se 1000 é 0dB, então 2000 é 6dB, 500 é -6dB.
    double bassGainDb = 20 * log10((double)gainsPtr[0] / 1000.0);
    double midGainDb = 20 * log10((double)gainsPtr[1] / 1000.0);
    double highGainDb = 20 * log10((double)gainsPtr[2] / 1000.0);


    // Configura os filtros
    bassFilter.setup(LOW_SHELF, bassGainDb, BASS_FREQ, Q_FACTOR, SAMPLE_RATE);
    midFilter.setup(PEAKING, midGainDb, MID_FREQ, Q_FACTOR, SAMPLE_RATE);
    highFilter.setup(HIGH_SHELF, highGainDb, HIGH_FREQ, Q_FACTOR, SAMPLE_RATE);

    ALOGD("NativeEqualizer: Processando %d amostras. Bass Gain: %.2fdB, Mid Gain: %.2fdB, High Gain: %.2fdB",
          numSamples, bassGainDb, midGainDb, highGainDb);

    // Aplica os filtros sequencialmente a cada amostra
    for (int i = 0; i < numSamples; i++) {
        double sample = audioDataPtr[i]; // Converte short para double para processamento de ponto flutuante

        // Aplica cada filtro em cascata
        sample = bassFilter.process(sample);
        sample = midFilter.process(sample);
        sample = highFilter.process(sample);

        // Converte de volta para short, com clipping para evitar estouro
        audioDataPtr[i] = static_cast<jshort>(fmax(SHRT_MIN, fmin(SHRT_MAX, sample)));
    }

    env->ReleaseShortArrayElements(audioData, audioDataPtr, 0);
    env->ReleaseIntArrayElements(gains, gainsPtr, JNI_ABORT);

    ALOGD("NativeEqualizer: Equalização multi-banda nativa aplicada com sucesso. Retornando numSamples: %d", numSamples);
    return numSamples;
}
