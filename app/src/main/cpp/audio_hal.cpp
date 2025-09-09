// --- Includes Padrão ---
#include <stdlib.h>     // Para funções de alocação de memória (malloc, free)
#include <string.h>     // Para funções de manipulação de strings (strcmp, memset)
#include <fcntl.h>      // Para flags de controle de arquivo (comum em HALs, mas não usado diretamente aqui)
#include <android/log.h> // Para a função __android_log_print, usada para logs no Logcat Android
#include <errno.h>      // Para códigos de erro padrão (ex: -ENODEV, -EINVAL, -ENOMEM)
#include <unistd.h>     // Para usleep (usado em passos futuros para simulação de otimização)
#include <stdio.h>      // Para printf, usado como log de fallback/verificação em C padrão


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
// Estas macros (ALOGD, ALOGE, ALOGI) são atalhos convenientes para a função
// __android_log_print, que envia mensagens para o Logcat do Android.
// Elas são definidas manualmente aqui para garantir sua disponibilidade,
// dado que o cabeçalho <log/log.h> (que as define nativamente) não foi encontrado
// de forma consistente no ambiente de build.
#ifndef ALOGD
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "AudioHAL", __VA_ARGS__)
#endif

#ifndef ALOGE
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, "AudioHAL", __VA_ARGS__)
#endif

#ifndef ALOGI
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, "AudioHAL", __VA_ARGS__)
#endif
// --- Fim das Definições Manuais das Macros ALOG ---


// --- Definições Mínimas Injetadas para Simulação da HAL ---
// Este bloco de código contém as definições mínimas de estruturas e macros
// que normalmente seriam fornecidas pelos cabeçalhos padrão do NDK,
// especificamente <hardware/hardware.h> e <hardware/audio.h>.
// Elas foram injetadas diretamente neste arquivo (e em native-lib.cpp)
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


// --- Estrutura Personalizada do Dispositivo de Áudio ---
// Esta estrutura estende a 'audio_hw_device_t' com campos adicionais
// para controle de estado da simulação.
// Ela DEVE VIR DEPOIS da definição de 'audio_hw_device_t' para que o tipo seja reconhecido.
typedef struct {
    audio_hw_device_t device; // Estrutura padrão do Android HAL, a ser preenchida e usada
    bool is_initialized;      // Flag para indicar se o dispositivo foi inicializado
    int error_count;          // Contador para simular erros (usado em passos futuros da atividade)
} custom_audio_device_t;


// --- Funções da HAL de Áudio Simulada ---
// Estas funções implementam o comportamento da sua HAL.
// Elas DEVE VIR ANTES das estruturas globais ('audio_module_methods' e 'HAL_MODULE_INFO_SYM')
// que as referenciam, devido à forma como o C/C++ processa o código (de cima para baixo).

/**
 * Função para fechar o dispositivo de áudio da HAL.
 * Libera a memória alocada para a instância do dispositivo.
 * Esta é uma função obrigatória na interface da HAL de hardware.
 * @param device Um ponteiro para a estrutura genérica do dispositivo de hardware a ser fechada.
 * @return 0 em caso de sucesso.
 */
static int audio_close(hw_device_t* device) {
    custom_audio_device_t* dev = (custom_audio_device_t*)device;
    if (dev) {
        free(dev); // Libera a memória alocada para a estrutura custom_audio_device_t
        ALOGD("AudioHAL: HAL de áudio liberada com sucesso."); // Log de depuração para o Logcat
    }
    return 0;
}

/**
 * Função para simular a escrita de dados de áudio na HAL.
 * Em uma HAL real, esta função processaria os dados de áudio recebidos (buffer e bytes).
 * Para esta simulação, ela apenas registra o "processamento" e pode simular falhas ou otimizações.
 * @param dev Um ponteiro para a estrutura do dispositivo de áudio que está processando a escrita.
 * @param buffer Um ponteiro para o buffer de dados de áudio a ser "processado".
 * @param bytes O número de bytes no buffer.
 * @return O número de bytes "processados" (simulando sucesso), ou um código de erro.
 */
static int audio_write(audio_hw_device_t* dev, const void* buffer, size_t bytes) {
    custom_audio_device_t* custom_dev = (custom_audio_device_t*)dev;

    // Verifica se a HAL está inicializada antes de "processar" dados.
    if (!custom_dev->is_initialized) {
        ALOGE("AudioHAL: Erro: HAL não inicializada!");
        return -ENODEV; // Retorna erro de dispositivo não encontrado (No Device)
    }

    // --- Simulação de Falhas (para o Passo 6 da atividade) ---
    // Descomente e ajuste este bloco quando for implementar o Passo 6.
    // Ele simula um erro após um certo número de chamadas para testar o diagnóstico.
    /*
    static int call_count = 0; // Contador estático para manter o estado entre chamadas
    call_count++;

    if (call_count > 5) { // Simula uma falha após a 5ª chamada bem-sucedida
        ALOGE("AudioHAL: ERRO SIMULADO: Falha após 5 chamadas (chamada #%d)", call_count);
        return -EIO; // Retorna um erro de I/O (Input/Output Error)
    }
    */

    // --- Otimização de Energia (para o Passo 8 da atividade) ---
    // Descomente esta linha quando for implementar o Passo 8.
    // Ela simula um pequeno atraso para reduzir a frequência de polling da CPU
    // e permitir que ela entre em estados de baixa energia mais frequentemente.
    // usleep(1000); // Pausa por 1 milissegundo (1000 microssegundos)

    // Log de depuração que indica o "processamento" dos dados de áudio.
    ALOGD("AudioHAL: Processando %zu bytes de áudio.", bytes);
    return bytes; // Retorna o número de bytes "processados" (simulando sucesso)
}

/**
 * Função para abrir e inicializar o dispositivo de áudio da HAL.
 * Esta é a função que o sistema Android chamará para "abrir" a sua HAL.
 * Ela aloca recursos e configura a estrutura do dispositivo.
 * @param module Um ponteiro para o módulo de hardware ao qual a HAL pertence.
 * @param name O nome da interface a ser aberta (ex: "primary" para o dispositivo de áudio principal).
 * @param device Um ponteiro para um ponteiro de dispositivo, onde a nova instância do dispositivo será armazenada.
 * @return 0 em caso de sucesso, ou um código de erro POSIX negativo (ex: -EINVAL, -ENOMEM).
 */
static int audio_open(const hw_module_t* module, const char* name, hw_device_t** device) {
    // Verifica se o nome da interface solicitada corresponde ao esperado para a HAL de áudio.
    if (strcmp(name, AUDIO_HARDWARE_INTERFACE) != 0) {
        ALOGE("AudioHAL: Erro: Interface %s não suportada!", name);
        return -EINVAL; // Retorna erro de argumento inválido
    }

    // Aloca memória para a estrutura personalizada do dispositivo de áudio.
    custom_audio_device_t* dev = (custom_audio_device_t*)malloc(sizeof(custom_audio_device_t));
    if (!dev) {
        ALOGE("AudioHAL: Falha na alocação de memória.");
        return -ENOMEM; // Retorna erro de falta de memória (No Memory)
    }

    memset(dev, 0, sizeof(custom_audio_device_t)); // Inicializa toda a memória alocada com zeros

    // Configuração da estrutura padrão 'common' do dispositivo de hardware.
    dev->device.common.tag = HARDWARE_DEVICE_TAG; // Identifica a estrutura como um dispositivo de hardware
    dev->device.common.version = AUDIO_DEVICE_API_VERSION_2_0; // Define a versão da API de áudio
    dev->device.common.module = (hw_module_t*)module; // Ponteiro de volta para o módulo HAL
    dev->device.common.close = audio_close; // Atribui a função de fechamento do dispositivo

    // Atribui as funções específicas da HAL de áudio.
    // Para esta simulação, apenas a função 'write' é atribuída.
    dev->device.write = audio_write; // Atribui a função de escrita de áudio (simulada)

    dev->is_initialized = true; // Marca a HAL como inicializada após a abertura bem-sucedida
    *device = (hw_device_t*)dev; // Retorna o ponteiro para a instância alocada e configurada do dispositivo

    ALOGD("AudioHAL: Dispositivo de áudio inicializado com sucesso."); // Log de sucesso para o Logcat
    return 0; // Retorna 0 para indicar sucesso
}


// --- Definição do Módulo HAL ---
// Estas estruturas são essenciais para que o sistema Android descubra e carregue
// a sua HAL a partir da biblioteca compartilhada (.so).
// Elas DEVE VIR DEPOIS das funções que referenciam (ex: 'audio_open'),
// devido à ordem de processamento do compilador C/C++.

// Estrutura de métodos do módulo.
// Contém o ponteiro para a função 'open' que o sistema usará para interagir com a HAL.
static struct hw_module_methods_t audio_module_methods = {
        .open = audio_open, // Atribui a função 'audio_open' que já foi definida acima
};

// Símbolo de Informação do Módulo HAL.
// Esta variável global com um nome específico (HAL_MODULE_INFO_SYM) é o que o sistema Android
// procura em bibliotecas compartilhadas para identificar e carregar módulos HAL.
// Ela contém metadados sobre a HAL e um ponteiro para seus métodos.
// O tipo 'struct audio_module' foi definido nas 'Definições Mínimas Injetadas'.
struct audio_module HAL_MODULE_INFO_SYM = {
        .common = {
                .tag = HARDWARE_MODULE_TAG,             // Tag para identificar como um módulo de hardware
                .version_major = 1,                     // Versão principal do seu módulo HAL (para o Android)
                .version_minor = 0,                     // Versão secundária do seu módulo HAL
                .id = AUDIO_HARDWARE_MODULE_ID,         // ID do módulo (conforme padrão Android para áudio: "audio")
                .name = "My Custom Audio HAL",          // Nome amigável do módulo (para logs e ferramentas)
                .author = "Seu Nome / Seu Grupo",       // Autor(es) do módulo (para identificação)
                .methods = &audio_module_methods,       // Ponteiro para a estrutura de métodos do módulo
                .dso = NULL,                            // Ponteiro para o objeto de biblioteca compartilhada (não aplicável para esta simulação estática)
        },
        // Nota: O campo '.common.open' não existe na estrutura 'hw_module_t' injetada.
        // A função 'open' é acessada através do ponteiro '.methods->open'.
        // A linha foi removida conforme as correções anteriores para garantir a compilação.
};
