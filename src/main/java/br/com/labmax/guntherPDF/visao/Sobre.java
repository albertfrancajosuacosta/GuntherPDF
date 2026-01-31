package br.com.labmax.guntherPDF.visao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import br.com.labmax.guntherPDF.util.Configurador;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class Sobre extends JDialog {

    private static final long serialVersionUID = 1L;

    // Ajuste estes valores conforme sua UI
    private static final int DIALOG_W = 520;
    private static final int MIN_TEXT_H = 90;     // altura mínima da área de texto
    private static final int MAX_TEXT_H = 190;    // até onde cresce antes de aparecer scroll
    private static final int IMG_W = 96;
    private static final int IMG_H = 96;

    public Sobre(Frame owner) {
        super(owner, "Sobre - GuntherPDF", true); // modal
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setContentPane(buildContent());
        pack();

        // centraliza após pack()
        setLocationRelativeTo(owner);

        // garante largura mínima (opcional)
        Dimension sz = getSize();
        if (sz.width < DIALOG_W) setSize(new Dimension(DIALOG_W, sz.height));
        setMinimumSize(new Dimension(DIALOG_W, getHeight()));
    }

    private JComponent buildContent() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));

        // ===== Header: imagem + título =====
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        ImageIcon icon = carregarImagem(Configurador.TITULO_FOTO_SOBE, IMG_W, IMG_H);
        JLabel imgLabel = new JLabel(icon != null ? icon : criarPlaceholderIcon(IMG_W, IMG_H));
        imgLabel.setVerticalAlignment(SwingConstants.TOP);

        JLabel title = new JLabel("GuntherPDF – Um visualizador de PDF.");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        //JLabel subtitle = new JLabel("LabMax - Laboratório Maximus\n\n");
        JLabel subtitle = new JLabel(
        	    "<html>LabMax – Laboratório Maximus</html>"
        	);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(new Color(120, 120, 120));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        header.add(imgLabel, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);

        // ===== Texto =====
        String conteudo =
                "O projeto GuntherPDF visa ao desenvolvimento de uma ferramenta em código livre destinada à visualização de arquivos em PDF.\n\n"
                + "Albert França Josuá Costa\n"
                + "Email: albertfrancajosuacosta@gmail.com\n"
                + "Linkedin: https://www.linkedin.com/in/albert-josu%C3%A1-9aa550239/\n"
                + "Github: https://github.com/albertfrancajosuacosta/\n\n\n";

        JTextArea text = new JTextArea(conteudo);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setBorder(null);
        text.setFocusable(false);

        // Auto-ajuste de altura do texto (cresce até MAX_TEXT_H; depois scroll)
        int viewportW = DIALOG_W - (12 + 12) - (12 + IMG_W) - 20; // margem/colunas aproximadas
        applyAutoHeight(text, Math.max(320, viewportW), MIN_TEXT_H, MAX_TEXT_H);

        JScrollPane scroll = new JScrollPane(text);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setOpaque(false);

        // Faz o ScrollPane respeitar a altura calculada do JTextArea
        Dimension prefTA = text.getPreferredSize();
        scroll.setPreferredSize(new Dimension(prefTA.width + 20, prefTA.height + 12));

        // ===== Footer =====
        JButton close = new JButton("Fechar");
        close.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(close);

        content.add(header, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);

        return content;
    }

    /**
     * Carrega imagem de forma dinâmica via classpath (src/main/resources).
     * Ex.: carregarImagem("/aluh.png", 96, 96)
     */
    private ImageIcon carregarImagem(String caminhoNoClasspath, int largura, int altura) {
        URL url = getClass().getResource(caminhoNoClasspath);
        if (url == null) {
            System.err.println("Imagem não encontrada em resources: " + caminhoNoClasspath);
            return null;
        }

        ImageIcon icon = new ImageIcon(url);
        Image img = icon.getImage().getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    /**
     * Ajusta a altura do JTextArea conforme o conteúdo, respeitando um mínimo e um máximo.
     * Se passar do máximo, o JScrollPane mostrará a barra vertical.
     */
    private static void applyAutoHeight(JTextArea ta, int width, int minH, int maxH) {
        // Importante: definir tamanho para o cálculo do preferredSize considerar quebras de linha.
        ta.setSize(new Dimension(width, Short.MAX_VALUE));

        Dimension pref = ta.getPreferredSize();
        int targetH = pref.height;

        if (targetH < minH) targetH = minH;
        if (targetH > maxH) targetH = maxH;

        ta.setPreferredSize(new Dimension(width, targetH));
        ta.setMinimumSize(new Dimension(width, minH));
        ta.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxH));
    }

    private static ImageIcon criarPlaceholderIcon(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(230, 230, 230));
        g.fillRoundRect(0, 0, w, h, 18, 18);
        g.setColor(new Color(120, 120, 120));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("sem imagem", 10, h / 2);
        g.dispose();
        return new ImageIcon(img);
    }
}
