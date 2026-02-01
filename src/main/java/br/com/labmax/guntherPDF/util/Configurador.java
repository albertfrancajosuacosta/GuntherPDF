package br.com.labmax.guntherPDF.util;

import java.awt.Color;
import java.awt.Dimension;

public final class Configurador {
	
	private Configurador() { }

    // ===== App / Títulos =====
    public static final String APP_TITLE = "GuntherPDF — Uma ferramenta simples para visualização de arquivos em PDF.";
    public static final String TITULO_FOTO_SOBE = "/gimel.png";
    
    // ===== Textos Toolbar =====
    public static final String TXT_OPEN = "Abrir";
    public static final String TXT_PAGE = "Página";
    public static final String TXT_ZOOM = "Zoom";
    public static final String TXT_FIT_WIDTH = "Ajustar Largura";
    public static final String TXT_FIT_PAGE = "Ajustar Página";
    public static final String TXT_SOBRE = "Sobre";

    // ===== Textos Sidebar =====
    public static final String TXT_PAGES_TITLE = "Páginas";
    
    // ===== Status / Dicas =====
    public static final String STATUS_READY = "Pronto";
    public static final String STATUS_OPENING_PREFIX = "Abrindo: ";
    public static final String STATUS_OPENED_PREFIX = "Aberto: ";
    public static final String STATUS_RENDERING_PREFIX = "Renderizando página ";
    public static final String STATUS_RENDERING_SUFFIX = "…";
    public static final String STATUS_PAGE_READY_PREFIX = "Página ";
    public static final String STATUS_PAGE_READY_SUFFIX = " pronta";
    public static final String STATUS_RENDER_ERROR = "Erro ao renderizar";
    public static final String STATUS_OPEN_ERROR = "Erro ao abrir PDF";
    public static final String STATUS_FIT_WIDTH = "Ajustado à largura";
    public static final String STATUS_FIT_PAGE = "Ajustado à página";
    public static final String STATUS_ZOOM_PREFIX = "Zoom: ";
    public static final String STATUS_ZOOM_SUFFIX = "%";

    //public static final String HINT_BAR = "Ctrl + Roda do mouse: Zoom | Ctrl+O: Abrir | Ctrl+F: Busca";
    public static final String HINT_BAR = "|Ctrl + Roda do mouse: Zoom | Ctrl+O: Abrir |";

    // ===== Símbolos / Botões =====
    public static final String SYM_PREV = "<";
    public static final String SYM_NEXT = ">";
    public static final String SYM_ZOOM_OUT = "−";
    public static final String SYM_ZOOM_IN = "+";
    public static final String SYM_SEARCH_PREV = "Up";
    public static final String SYM_SEARCH_NEXT = "Down";

    // ===== Mensagens =====
    public static final String DLG_OPEN_TITLE = "Abrir PDF";
    public static final String DLG_OPEN_FILTER_NAME = "PDF (*.pdf)";
    public static final String DLG_ERROR_TITLE = "Erro";
    public static final String MSG_OPEN_FAIL_PREFIX = "Falha ao abrir o PDF:\n";
    public static final String MSG_RENDER_FAIL_PREFIX = "Falha ao renderizar página:\n";
    public static final String MSG_EMPTY_VIEW = "Abra um PDF (Ctrl+O) para visualizar.";

    // Texto para imagem de erro renderização
    public static final String ERRIMG_TITLE = "Falha ao renderizar a página";
    public static final String ERRIMG_BODY =
            "O PDF parece conter instruções gráficas inválidas (PDFBox foi mais rígido).\n" +
            "Sugestões:\n" +
            "• Tente 'Salvar como' / 'Imprimir em PDF' no Adobe/Edge/Chrome e reabra.\n" +
            "• Se for um PDF gerado por sistema, pode estar corrompido.\n\n" +
            "Detalhe: ";

    // =====================================================================================
    // =============================== PALETA CLARA ========================================
    // =====================================================================================

    // Superfícies
    public static final Color C_ROOT_BG   = Color.decode("#F2F4F7"); // fundo geral
    public static final Color C_TOPBAR_BG = Color.decode("#FFFFFF"); // barra superior
    public static final Color C_RIGHT_BG  = Color.decode("#FFFFFF"); // painéis laterais
    public static final Color C_CARD_BG   = Color.decode("#FFFFFF"); // futuro (cards etc.)

    // Contornos / separadores
    public static final Color C_BORDER = Color.decode("#D0D7DE");    // linhas de divisão
    public static final Color C_SHADOW = new Color(0, 0, 0, 25);     // sombra leve (alpha)

    // Texto
    public static final Color C_TEXT        = Color.decode("#1F2328"); // texto principal
    public static final Color C_TEXT_DIM    = Color.decode("#57606A"); // texto secundário
    public static final Color C_TEXT_BRIGHT = Color.decode("#0B0F14"); // “forte” (títulos)

    // Botões
    public static final Color C_BUTTON_BG      = Color.decode("#F6F8FA");
    public static final Color C_BUTTON_HOVER   = Color.decode("#EEF2F6");
    public static final Color C_BUTTON_PRESSED = Color.decode("#E3E8EF");

    // Lista (páginas)
    public static final Color C_LIST_FG      = Color.decode("#1F2328");
    public static final Color C_LIST_SEL_BG  = Color.decode("#DDEBFF"); // seleção clara
    public static final Color C_LIST_SEL_FG  = Color.decode("#0B3D91"); // texto seleção

    // Campo de texto (busca/página)
    public static final Color C_FIELD_BG     = Color.decode("#FFFFFF");
    public static final Color C_FIELD_BORDER = Color.decode("#C7D0DB");
    public static final Color C_CARET        = Color.decode("#0B3D91");

    // “Página” (área do documento)
    public static final Color C_CANVAS_BG = Color.decode("#E9EEF3"); // fundo atrás da página
    public static final Color C_PAGE_BG   = Color.decode("#FFFFFF"); // página em si
    public static final Color C_PAGE_EDGE = Color.decode("#C9D1D9"); // borda suave da página

    // ===== Dimensões =====
    public static final Dimension MIN_SIZE = new Dimension(1040, 700);
    public static final int DIVIDER_SIZE = 6;

    public static final int LIST_CELL_H = 34;
}


