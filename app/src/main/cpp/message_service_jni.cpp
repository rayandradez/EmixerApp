// --- Includes Padrão ---
#include <jni.h>         // Para funções JNI (Java Native Interface)
#include <string>        // Para manipulação de strings C++
#include <android/log.h> // Para a função __android_log_print, usada para logs no Logcat Android
#include <string.h>      // Para funções de manipulação de memória (ex: memset)
#include <stdio.h>       // Para printf, usado como log de fallback/verificação em C padrão


// --- Definições Manuais de Constantes de Prioridade de Log ---
// Estas constantes (ANDROID_LOG_DEBUG, ANDROID_LOG_ERROR, ANDROID_LOG_INFO)
// são parte do cabeçalho <android/log.h> do NDK. No entanto, devido a problemas
// de ambiente que impediram sua detecção automática pelo compilador, elas são
// definidas explicitamente aqui para garantir que as macros ALOG funcionem.
#ifndef ANDROID_LOG_DEBUG
#define ANDROID_LOG_DEBUG 3 // Prioridade de depuração (Debug)
#endif
#ifndef ANDROID_LOG_ERROR
#define ANDROID_LOG_ERROR 6 // Prioridade de erro (Error)
#endif
#ifndef ANDROID_LOG_INFO
#define ANDROID_LOG_INFO 4  // Prioridade de informação (Info)
#endif


// --- Definições Manuais das Macros ALOG ---
// Estas macros (ALOGD, ALOGE) são atalhos convenientes para a função
// __android_log_print, que envia mensagens para o Logcat do Android.
// Elas são definidas manualmente aqui para garantir sua disponibilidade,
// dado que o cabeçalho <log/log.h> (que as define nativamente) não foi encontrado
// de forma consistente no ambiente de build.
#ifndef ALOGD
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "NativeJNI", __VA_ARGS__)
#endif

#ifndef ALOGE
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, "NativeJNI", __VA_ARGS__)
#endif


// --- Definições Mínimas Injetadas para Simulação da HAL ---
// Este bloco de código contém as definições mínimas de estruturas e macros
// que normalmente seriam fornecidas pelos cabeçalhos padrão do NDK,
// especificamente <hardware/hardware.h> e <hardware/audio.h>.
// Elas foram injetadas diretamente neste arquivo (e em audio_hal.cpp)
// como uma solução de contorno para problemas de ambiente que impediram
// o compilador de encontrar esses cabeçalhos.
// A ordem de declaração das estruturas é crucial em C/C++ para evitar erros de tipo.

// Forward declarations para estruturas que se referenciam mutuamente.
// Isso informa ao compilador sobre a existência dessas estruturas antes de suas definições completas.
struct hw_module_t;
struct hw_device_t;
struct audio_hw_device; // Forward declaration para a estrutura base de audio_hw_device_t

// Estrutura comum para todos os módulos de hardware (base para HALs).
// Define a interface básica que um módulo HAL deve ter.
struct hw_module_t {
    uint32_t tag;               // Identificador único da estrutura (ex: HARDWARE_MODULE_TAG)
    uint32_t version_major;     // Versão principal da interface do módulo
    uint32_t version_minor;     // Versão secundária da interface do módulo
    const char *id;             // Identificador textual do módulo (ex: "audio")
    const char *name;           // Nome amigável do módulo
    const char *author;         // Autor ou organização responsável pelo módulo
    struct hw_module_methods_t *methods; // Ponteiro para os métodos de acesso do módulo
    void* dso;                  // Ponteiro interno para o objeto de biblioteca compartilhada (não usado na simulação)
    uint32_t reserved[32-7];    // Campos reservados para uso futuro, garantindo tamanho fixo
};

// Métodos comuns para todos os módulos de hardware.
// Contém o método 'open', que é o ponto de entrada principal para obter uma instância de um dispositivo HAL.
struct hw_module_methods_t {
    // Função para abrir um dispositivo de hardware específico dentro do módulo.
    // Parâmetros: ponteiro para o módulo, ID do dispositivo (ex: "primary"), e ponteiro para preencher com o dispositivo aberto.
    int (*open)(const struct hw_module_t* module, const char* id, struct hw_device_t** device);
};

// Estrutura comum para todos os dispositivos de hardware.
// Define a interface básica que um dispositivo HAL deve ter.
struct hw_device_t {
    uint32_t tag;               // Identificador único da estrutura (ex: HARDWARE_DEVICE_TAG)
    uint32_t version;           // Versão da interface do dispositivo
    struct hw_module_t* module; // Ponteiro de volta para o módulo ao qual este dispositivo pertence
    int (*close)(struct hw_device_t* device); // Função para fechar e liberar o dispositivo
    void* reserved[32-4];       // Campos reservados para uso futuro
};

// Estrutura específica do dispositivo de áudio.
// Estende a estrutura genérica 'hw_device_t' com operações específicas de áudio.
// 'audio_hw_device_t' é um alias (typedef) para 'struct audio_hw_device'.
typedef struct audio_hw_device {
    struct hw_device_t common; // Membro comum, herdando as propriedades e métodos de 'hw_device_t'

    // Operações específicas de áudio.
    // Para esta simulação, apenas a função 'write' (simulando a saída de áudio) é implementada.
    int (*write)(struct audio_hw_device* dev, const void* buffer, size_t bytes);

    void* reserved[32 - 1]; // Campos reservados para outras funções de áudio não simuladas
} audio_hw_device_t;

// Estrutura para o módulo de áudio.
// Usada para definir o símbolo HAL_MODULE_INFO_SYM que o Android procura em bibliotecas compartilhadas.
// 'audio_module_t' é um alias (typedef) para 'struct audio_module'.
typedef struct audio_module {
    struct hw_module_t common; // Membro comum, herdando as propriedades de 'hw_module_t'
} audio_module_t;

// Macros e Constantes cruciais para a identificação e versão das HALs.
#define HARDWARE_MODULE_TAG        0x4D4F4455 // "MODU" - Tag para módulos de hardware (ASCII para MODU)
#define HARDWARE_DEVICE_TAG        0x44455649 // "DEVI" - Tag para dispositivos de hardware (ASCII para DEVI)

#define AUDIO_DEVICE_API_VERSION_2_0 0x02000000 // Versão da API do dispositivo de áudio (Major 2, Minor 0)

#define AUDIO_HARDWARE_MODULE_ID   "audio"    // ID padrão para o módulo HAL de áudio (usado por hw_get_module)
#define AUDIO_HARDWARE_INTERFACE   "primary"  // Nome da interface primária de áudio (comum para áudio)

// --- Fim das Definições Injetadas ---


// --- Variáveis Globais para a HAL ---
// Ponteiros estáticos para o módulo e dispositivo de áudio da HAL.
// 'static' garante que estas variáveis são visíveis apenas dentro deste arquivo.
// Elas são inicializadas como NULL e preenchidas na primeira chamada JNI.
static audio_module_t* gAudioModule = NULL;
static audio_hw_device_t* gAudioDevice = NULL;


// --- Função JNI Principal ---
/**
 * Ponto de entrada JNI do lado C++ para interagir com a HAL de áudio simulada.
 * Esta função é chamada a partir do Kotlin/Java para acionar o processo de áudio.
 *
 * @param env Ponteiro para a interface JNI (Java Native Interface Environment).
 * @param obj O objeto Java/Kotlin que chamou este método (neste caso, a instância de MainActivity).
 *            O parâmetro 'jobject' é renomeado para 'obj' e marcado como não utilizado
*            para evitar warnings do compilador, já que não precisamos acessar seus membros.
* @return Um inteiro que indica o sucesso (0) ou falha (código de erro) da operação da HAL.
*/
extern "C" JNIEXPORT jint JNICALL
Java_com_example_emixerapp_MessageService_triggerHalAudioWrite( // <<< ALTERADO AQUI
        JNIEnv* env,
        jobject /*obj*/) {

    // Log de verificação usando printf. Útil para depuração em ambientes onde ALOG não funciona.
    printf("NATIVE_LIB: triggerHalAudioWrite - INICIANDO\n");
    // Log detalhado para o Logcat Android, usando a macro ALOGD definida acima.
    ALOGD("JNI: Tentando chamar triggerHalAudioWrite...");

    // Declaração externa do símbolo HAL_MODULE_INFO_SYM.
    // Este símbolo representa a instância global da sua HAL de áudio, definida em audio_hal.cpp.
    // 'extern' informa ao compilador que essa variável existe, mas está definida em outro arquivo.
    extern struct audio_module HAL_MODULE_INFO_SYM;

    // 1. Obter o Módulo HAL de Áudio
    // Esta lógica simula o processo de carregamento de um módulo HAL.
    // Em um sistema Android real, a função 'hw_get_module()' seria usada para carregar
    // dinamicamente o módulo da biblioteca compartilhada (.so).
    // Aqui, como estamos em um ambiente de simulação e contorno de problemas de NDK,
    // referenciamos diretamente a instância global da HAL definida em 'audio_hal.cpp'.
    if (gAudioModule == NULL) {
        gAudioModule = &HAL_MODULE_INFO_SYM; // Atribui o endereço da instância global da HAL
        ALOGD("JNI: Módulo de áudio HAL obtido via referência direta.");
    }

    // 2. Abrir o Dispositivo HAL de Áudio
    // Esta lógica simula a abertura de um dispositivo específico dentro do módulo HAL.
    // A função 'open' do módulo HAL é chamada para obter a instância do dispositivo de áudio.
    if (gAudioDevice == NULL) {
        // Chama o método 'open' através da estrutura 'methods' do módulo HAL.
        // 'AUDIO_HARDWARE_INTERFACE' identifica a interface de áudio a ser aberta (ex: "primary").
        int ret = gAudioModule->common.methods->open((const hw_module_t*)gAudioModule,
                                                     AUDIO_HARDWARE_INTERFACE,
                                                     (hw_device_t**)&gAudioDevice);
        if (ret != 0) {
            // Em caso de falha na abertura do dispositivo, registra um erro e reseta o ponteiro do módulo.
            ALOGE("JNI: Falha ao abrir o dispositivo de áudio HAL: %d", ret);
            gAudioModule = NULL; // Reseta para tentar novamente em futuras chamadas JNI
            return ret;          // Retorna o código de erro para o Kotlin/Java
        }
        ALOGD("JNI: Dispositivo de áudio HAL aberto com sucesso.");
    }

    // 3. Chamar a Função audio_write da HAL (Simulada)
    // Prepara um buffer de dados fictício (dummy_buffer) para simular o envio de dados de áudio para a HAL.
    unsigned char dummy_buffer[1024];
    memset(dummy_buffer, 0, sizeof(dummy_buffer)); // Preenche o buffer com zeros para dados limpos

    // Chama a função 'write' do dispositivo de áudio da HAL.
    // Esta função simula o processamento dos dados de áudio pela HAL.
    ssize_t bytes_written = gAudioDevice->write(gAudioDevice, dummy_buffer, sizeof(dummy_buffer));

    // Verifica o resultado da operação de escrita na HAL.
    if (bytes_written >= 0) {
        // Se 'bytes_written' for não-negativo, a operação é considerada bem-sucedida.
        ALOGD("JNI: Função audio_write da HAL chamada. Bytes processados: %zd", bytes_written);
        return 0; // Retorna 0 para indicar sucesso ao Kotlin/Java
    } else {
        // Se 'bytes_written' for negativo, indica um erro retornado pela HAL.
        ALOGE("JNI: Erro ao chamar audio_write da HAL: %zd", bytes_written);
        return (jint)bytes_written; // Retorna o código de erro para o Kotlin/Java
    }
}
