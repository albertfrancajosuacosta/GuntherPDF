package br.com.labmax.guntherPDF.visao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;


import br.com.labmax.guntherPDF.util.Configurador;


public class Visualizador extends JFrame {

	private static final long serialVersionUID = 1L;
	//private JPanel contentPane;
	
	private PDDocument document;
    private PDFRenderer renderer;

    private int paginaAtual = 1;      // 1-based
    private int totalPaginas = 0; //totalPages
    private int zoomPercent = 100;    // 25..400

    private final JTextField pageField = new JTextField("1", 3);
    private final JLabel totalPagesLabel = new JLabel("/ 0");
    private final JLabel zoomLabel = new JLabel("100%");
    private final JLabel statusLabel = new JLabel(Configurador.STATUS_READY);

    
   
    

    private final DefaultListModel<Integer> pagesModel = new DefaultListModel<>();
    private final JList<Integer> pagesList = new JList<>(pagesModel);

    private final PageView pageView = new PageView();
    private final JScrollPane pageScroll = new JScrollPane(pageView);

    private final Map<String, BufferedImage> pageCache = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
            return size() > 24;
        }
    };
	
	
	/**
	 * Create the frame.
	 */
	public Visualizador() {
		super(Configurador.APP_TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationByPlatform(true);
		setContentPane(buildRoot());
		wireActions();

        pageScroll.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                e.consume();
                int rot = e.getWheelRotation();
                if (rot < 0) setZoom(zoomPercent + 10);
                else setZoom(zoomPercent - 10);
            }
        });

        pack();
	}
	
	private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Configurador.C_ROOT_BG);

        root.add(buildTopToolbar(), BorderLayout.NORTH);
        root.add(buildMainSplit(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        return root;
    }
	
	private JComponent buildTopToolbar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new MatteBorder(0, 0, 1, 0, Configurador.C_BORDER));
        top.setBackground(Configurador.C_TOPBAR_BG);

        JPanel bar = new JPanel();
        bar.setOpaque(false);
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.setBorder(new EmptyBorder(8, 10, 8, 10));

        JButton btnOpen = makeToolButton(Configurador.TXT_OPEN);
        JButton btnPrev = makeToolButton(Configurador.SYM_PREV);
        JButton btnNext = makeToolButton(Configurador.SYM_NEXT);
        JButton btnZoomOut = makeToolButton(Configurador.SYM_ZOOM_OUT);
        JButton btnZoomIn = makeToolButton(Configurador.SYM_ZOOM_IN);
        JButton btnFitWidth = makeToolButton(Configurador.TXT_FIT_WIDTH);
        JButton btnFitPage = makeToolButton(Configurador.TXT_FIT_PAGE);
        JButton btnSobre = makeToolButton(Configurador.TXT_SOBRE);

        btnOpen.putClientProperty("action", "abrir");
        btnPrev.putClientProperty("action", "anterior");
        btnNext.putClientProperty("action", "proxima");
        btnZoomOut.putClientProperty("action", "zoomOut");
        btnZoomIn.putClientProperty("action", "zoomIn");
        btnFitWidth.putClientProperty("action", "ajustarLargura");
        btnFitPage.putClientProperty("action", "ajustarPagina");
        btnSobre.putClientProperty("action", "sobre");
        
        
        

        ActionListener buttonListener = e -> {
            Object src = e.getSource();
            if (!(src instanceof JComponent c)) return;
            Object a = c.getClientProperty("action");
            if (a == null) return;
             handleAction(a.toString());
        };

        btnOpen.addActionListener(buttonListener);
        btnPrev.addActionListener(buttonListener);
        btnNext.addActionListener(buttonListener);
        btnZoomOut.addActionListener(buttonListener);
        btnZoomIn.addActionListener(buttonListener);
        btnFitWidth.addActionListener(buttonListener);
        btnFitPage.addActionListener(buttonListener);
        btnSobre.addActionListener(buttonListener);

        pageField.setHorizontalAlignment(SwingConstants.CENTER);
        pageField.setMaximumSize(new Dimension(60, 28));
        totalPagesLabel.setForeground(Configurador.C_TEXT);
        zoomLabel.setForeground(Configurador.C_TEXT);

        bar.add(btnOpen);
        bar.add(Box.createHorizontalStrut(14));
        bar.add(makeSeparator());

        bar.add(Box.createHorizontalStrut(10));
        bar.add(btnPrev);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(btnNext);
        bar.add(Box.createHorizontalStrut(10));
        bar.add(makeLabel(Configurador.TXT_PAGE));
        bar.add(Box.createHorizontalStrut(6));
        bar.add(pageField);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(totalPagesLabel);

        bar.add(Box.createHorizontalStrut(14));
        bar.add(makeSeparator());

        bar.add(Box.createHorizontalStrut(10));
        bar.add(btnZoomOut);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(btnZoomIn);
        bar.add(Box.createHorizontalStrut(10));
        bar.add(makeLabel(Configurador.TXT_ZOOM));
        bar.add(Box.createHorizontalStrut(6));
        bar.add(zoomLabel);

        bar.add(Box.createHorizontalStrut(14));
        bar.add(makeSeparator());
        bar.add(Box.createHorizontalStrut(10));
        bar.add(btnFitWidth);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(btnFitPage);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(btnSobre);

        bar.add(Box.createHorizontalGlue());

        top.add(bar, BorderLayout.CENTER);
        return top;
    }
	
	private JButton makeToolButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 10, 6, 10));
        b.setForeground(Configurador.C_TEXT_BRIGHT);
        b.setBackground(Configurador.C_BUTTON_BG);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 12f));
        b.setBorderPainted(false);

        b.addChangeListener(e -> {
            ButtonModel m = b.getModel();
            if (m.isPressed()) b.setBackground(Configurador.C_BUTTON_PRESSED);
            else if (m.isRollover()) b.setBackground(Configurador.C_BUTTON_HOVER);
            else b.setBackground(Configurador.C_BUTTON_BG);
        });

        return b;
    }
	
	
	private JComponent buildStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(Configurador.C_TOPBAR_BG);
        status.setBorder(new MatteBorder(1, 0, 0, 0, Configurador.C_BORDER));
        status.setPreferredSize(new Dimension(10, 28));

        statusLabel.setForeground(Configurador.C_TEXT);
        statusLabel.setBorder(new EmptyBorder(0, 10, 0, 10));

        JLabel hint = new JLabel(Configurador.HINT_BAR);
        hint.setForeground(Configurador.C_TEXT_DIM);
        hint.setBorder(new EmptyBorder(0, 10, 0, 10));

        status.add(statusLabel, BorderLayout.WEST);
        status.add(hint, BorderLayout.EAST);

        return status;
    }
	
	private JComponent makeSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 22));
        sep.setMaximumSize(new Dimension(1, 22));
        sep.setForeground(Configurador.C_BORDER);
        sep.setBackground(Configurador.C_BORDER);
        return sep;
    }
	
	private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Configurador.C_TEXT);
        l.setBorder(new EmptyBorder(0, 2, 0, 2));
        return l;
    }
	
	private JComponent buildMainSplit() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBorder(null);
        split.setDividerSize(Configurador.DIVIDER_SIZE);
        split.setContinuousLayout(true);
        split.setLeftComponent(buildViewer());
        split.setRightComponent(buildRightColumnSplit());
        split.setResizeWeight(0.78);
        split.setDividerLocation(780);
        return split;
    }
	
	private JComponent buildViewer() {
        JPanel viewer = new JPanel(new BorderLayout());
        viewer.setBackground(Configurador.C_ROOT_BG);
        pageScroll.setBorder(null);
        pageScroll.getViewport().setBackground(Configurador.C_ROOT_BG);
        pageScroll.getVerticalScrollBar().setUnitIncrement(16);
        pageScroll.getHorizontalScrollBar().setUnitIncrement(16);
        viewer.add(pageScroll, BorderLayout.CENTER);
        return viewer;
    }
	
	private JComponent buildRightColumnSplit() {
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setBorder(null);
        rightSplit.setDividerSize(Configurador.DIVIDER_SIZE);
        rightSplit.setContinuousLayout(true);

        //rightSplit.setTopComponent(buildSearchPanel());
        rightSplit.setBottomComponent(buildPagesPanel());

        rightSplit.setResizeWeight(0.5);
        rightSplit.setDividerLocation(0.5);

        return rightSplit;
    }
    
    /*
     * 
     
    private void styleSmallButton(JButton b) {
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 10, 6, 10));
        b.setForeground(Configurador.C_TEXT_BRIGHT);
        b.setBackground(Configurador.C_BUTTON_BG);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 12f));
        b.setBorderPainted(false);

        b.addChangeListener(e -> {
            ButtonModel m = b.getModel();
            if (m.isPressed()) b.setBackground(Configurador.C_BUTTON_PRESSED);
            else if (m.isRollover()) b.setBackground(Configurador.C_BUTTON_HOVER);
            else b.setBackground(Configurador.C_BUTTON_BG);
        });
    }
    */
    private JComponent buildPagesPanel() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Configurador.C_RIGHT_BG);
        bottom.setBorder(new MatteBorder(0, 1, 0, 0, Configurador.C_BORDER));

        JLabel title = new JLabel(Configurador.TXT_PAGES_TITLE);
        title.setBorder(new EmptyBorder(10, 10, 10, 10));
        title.setForeground(Configurador.C_TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD));

        pagesList.setBackground(Configurador.C_RIGHT_BG);
        pagesList.setForeground(Configurador.C_LIST_FG);
        pagesList.setSelectionBackground(Configurador.C_LIST_SEL_BG);
        pagesList.setSelectionForeground(Configurador.C_LIST_SEL_FG);
        pagesList.setFixedCellHeight(Configurador.LIST_CELL_H);
        pagesList.setBorder(new EmptyBorder(6, 6, 6, 6));

        pagesList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                         Object value,
                                                         int index,
                                                         boolean isSelected,
                                                         boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setText(Configurador.TXT_PAGE + " " + (index + 1));
                label.setBorder(new EmptyBorder(6, 12, 6, 6));
                label.setIcon(null);
                return label;
            }
        });

        JScrollPane sp = new JScrollPane(pagesList);
        sp.setBorder(null);
        sp.getViewport().setBackground(Configurador.C_RIGHT_BG);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        bottom.add(title, BorderLayout.NORTH);
        bottom.add(sp, BorderLayout.CENTER);

        return bottom;
    }
    
   /*
        cb.setOpaque(false);
        cb.setForeground(Configurador.C_TEXT);
        cb.setFocusPainted(false);
        cb.setAlignmentY(Component.CENTER_ALIGNMENT);
    }
    */
    
    
    private static class PageView extends JPanel {
        private BufferedImage currentImage;

        PageView() {
            setBackground(Configurador.C_ROOT_BG);
            setOpaque(true);
            setBorder(new EmptyBorder(30, 30, 30, 30));
        }

        BufferedImage getCurrentImage() {
            return currentImage;
        }

        void setImage(BufferedImage img) {
            this.currentImage = img;
        }

        @Override
        public Dimension getPreferredSize() {
            if (currentImage == null) return new Dimension(800, 1000);
            return new Dimension(currentImage.getWidth() + 60, currentImage.getHeight() + 60);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            // ✅ CORREÇÃO: hints compatíveis (evita IllegalArgumentException)
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // opcional (qualidade):
            // g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            if (currentImage == null) {
                g2.setColor(Configurador.C_TEXT);
                g2.drawString(Configurador.MSG_EMPTY_VIEW, 40, 60);
                g2.dispose();
                return;
            }

            int pad = 30;
            int w = getWidth();
            int h = getHeight();
            int pageW = currentImage.getWidth();
            int pageH = currentImage.getHeight();

            int x = Math.max(pad, (w - pageW) / 2);
            int y = Math.max(pad, (h - pageH) / 2);

            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(x + 8, y + 10, pageW, pageH, 10, 10);

            g2.drawImage(currentImage, x, y, null);

            g2.setColor(new Color(0, 0, 0, 35));
            g2.drawRoundRect(x, y, pageW, pageH, 10, 10);

            g2.dispose();
        }
    }
    
    private void wireActions() {
        pagesList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int idx = pagesList.getSelectedIndex();
            if (idx >= 0) setCurrentPage(idx + 1, true);
        });

        pageField.addActionListener(e -> {
            String t = pageField.getText().trim();
            try {
                int p = Integer.parseInt(t);
                setCurrentPage(p, true);
            } catch (NumberFormatException ex) {
                Toolkit.getDefaultToolkit().beep();
                pageField.setText(String.valueOf(paginaAtual));
            }
        });

        

        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "paginaAnterior"); //prevPage
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "paginaProxima"); //nextPage
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "abrir");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK), "zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK), "zoomOut");

        am.put("paginaAnterior", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { setCurrentPage(paginaAtual - 1, true); }});
        am.put("paginaProxima", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { setCurrentPage(paginaAtual + 1, true); }});
        am.put("abrir", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { handleAction("abrir"); }});
        am.put("zoomIn", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { setZoom(zoomPercent + 10); }});
        am.put("zoomOut", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { setZoom(zoomPercent - 10); }});

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                closeDocumentQuietly();
            }
        });
    }
    
   
    private void handleAction(String action) {
        switch (action) {
            case "abrir" -> openPdfChooser();
            case "anterior" -> setCurrentPage(paginaAtual - 1, true);//currentPage
            case "proxima" -> setCurrentPage(paginaAtual + 1, true);
            case "zoomOut" -> setZoom(zoomPercent - 10);
            case "zoomIn" -> setZoom(zoomPercent + 10);
            case "ajustarLargura" -> fitToWidth();
            case "ajustarPagina" -> fitToPage();
            case "sobre" -> openSobre();
            default -> { }
        }
    }
    
    

    private void openPdfChooser() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(Configurador.DLG_OPEN_TITLE);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Configurador.DLG_OPEN_FILTER_NAME, "pdf"));
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            loadPdf(fc.getSelectedFile());
        }
    }

    public void loadPdf(File file) {
        statusLabel.setText(Configurador.STATUS_OPENING_PREFIX + file.getName());

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                closeDocumentQuietly();
                //document = PDDocument.load(file);
                document = PDDocument.load(file, MemoryUsageSetting.setupTempFileOnly());
                renderer = new PDFRenderer(document);
                renderer.setSubsamplingAllowed(true);

                totalPaginas = document.getNumberOfPages();
                synchronized (pageCache) { pageCache.clear(); }
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    buildPagesList();
                    setCurrentPage(1, false);

                    statusLabel.setText(Configurador.STATUS_OPENED_PREFIX + file.getName());

               
                } catch (Exception ex) {
                    statusLabel.setText(Configurador.STATUS_OPEN_ERROR);
                 
                    showError(Configurador.MSG_OPEN_FAIL_PREFIX + ex.getMessage());
                }
            }
        }.execute();
    }

    private void buildPagesList() {
        pagesModel.clear();
        for (int i = 1; i <= totalPaginas; i++) pagesModel.addElement(i);
        totalPagesLabel.setText("/ " + totalPaginas);
        if (totalPaginas > 0) pagesList.setSelectedIndex(0);
    }

    private void setCurrentPage(int page, boolean scrollToTop) {
        if (totalPaginas <= 0) return;

        paginaAtual = Math.max(1, Math.min(totalPaginas, page));
        pageField.setText(String.valueOf(paginaAtual));
        zoomLabel.setText(zoomPercent + "%");

        int idx = paginaAtual - 1;
        if (pagesList.getSelectedIndex() != idx) pagesList.setSelectedIndex(idx);
        pagesList.ensureIndexIsVisible(idx);

        renderPageAsync(paginaAtual, zoomPercent, scrollToTop);
    }

    private void setZoom(int zoom) {
        if (totalPaginas <= 0) return;
        zoomPercent = Math.max(25, Math.min(400, zoom));
        zoomLabel.setText(zoomPercent + "%");
        renderPageAsync(paginaAtual, zoomPercent, false);
        statusLabel.setText(Configurador.STATUS_ZOOM_PREFIX + zoomPercent + Configurador.STATUS_ZOOM_SUFFIX);
    }

    private void fitToWidth() {
        BufferedImage img = pageView.getCurrentImage();
        if (img == null) { setZoom(100); return; }

        int viewportW = pageScroll.getViewport().getWidth();
        if (viewportW <= 50) return;

        double currentScale = zoomPercent / 100.0;
        double baseWidthAt100 = img.getWidth() / currentScale;

        int targetW = Math.max(200, viewportW - 60);
        int newZoom = (int) Math.round((targetW / baseWidthAt100) * 100.0);
        setZoom(newZoom);
        statusLabel.setText(Configurador.STATUS_FIT_WIDTH);
    }
    
    private void openSobre() {
    	SwingUtilities.invokeLater(() -> {
			new Sobre(this).setVisible(true);
		});
    }

    private void fitToPage() {
        BufferedImage img = pageView.getCurrentImage();
        if (img == null) { setZoom(100); return; }

        int viewportW = pageScroll.getViewport().getWidth();
        int viewportH = pageScroll.getViewport().getHeight();
        if (viewportW <= 50 || viewportH <= 50) return;

        double currentScale = zoomPercent / 100.0;
        double baseW = img.getWidth() / currentScale;
        double baseH = img.getHeight() / currentScale;

        int targetW = Math.max(200, viewportW - 60);
        int targetH = Math.max(200, viewportH - 60);

        double scale = Math.min(targetW / baseW, targetH / baseH);
        int newZoom = (int) Math.round(scale * 100.0);
        setZoom(newZoom);
        statusLabel.setText(Configurador.STATUS_FIT_PAGE);
    }

    private void renderPageAsync(int pageNumber1Based, int zoomPercent, boolean scrollToTop) {
        if (renderer == null) return;

        int pageIndex = pageNumber1Based - 1;
        String key = pageIndex + "@" + zoomPercent;

        BufferedImage cached;
        synchronized (pageCache) { cached = pageCache.get(key); }
        if (cached != null) {
            pageView.setImage(cached);
            pageView.revalidate();
            pageView.repaint();
            if (scrollToTop) SwingUtilities.invokeLater(() -> pageScroll.getVerticalScrollBar().setValue(0));
            return;
        }

        statusLabel.setText(Configurador.STATUS_RENDERING_PREFIX + pageNumber1Based + Configurador.STATUS_RENDERING_SUFFIX);

        new SwingWorker<BufferedImage, Void>() {
            @Override protected BufferedImage doInBackground() {
                try {
                    float dpi = 72f * (zoomPercent / 100f);
                    return renderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB);
                } catch (Exception ex) {
                    return makeErrorImage(
                    		Configurador.ERRIMG_TITLE,
                    		Configurador.ERRIMG_BODY + ex.getClass().getSimpleName() + " — " + String.valueOf(ex.getMessage())
                    );
                }
            }

            @Override protected void done() {
                try {
                    BufferedImage img = get();
                    synchronized (pageCache) { pageCache.put(key, img); }

                    if (paginaAtual == pageNumber1Based && Visualizador.this.zoomPercent == zoomPercent) {
                        pageView.setImage(img);
                        pageView.revalidate();
                        pageView.repaint();
                        if (scrollToTop) SwingUtilities.invokeLater(() -> pageScroll.getVerticalScrollBar().setValue(0));
                        statusLabel.setText(Configurador.STATUS_PAGE_READY_PREFIX + paginaAtual + Configurador.STATUS_PAGE_READY_SUFFIX);
                    }
                } catch (Exception ex) {
                    statusLabel.setText(Configurador.STATUS_RENDER_ERROR);
                    showError(Configurador.MSG_RENDER_FAIL_PREFIX + ex.getMessage());
                }
            }
        }.execute();
    }

    private void closeDocumentQuietly() {
        if (document != null) {
            try { document.close(); } catch (IOException ignored) { }
        }
        document = null;
        renderer = null;
        totalPaginas = 0;
        paginaAtual = 1;

        pageView.setImage(null);
        pagesModel.clear();
        totalPagesLabel.setText("/ 0");

      
  
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, Configurador.DLG_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }

    private static BufferedImage makeErrorImage(String line1, String line2) {
        int w = 700, h = 900;
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(220, 220, 220));
        g2.drawRect(10, 10, w - 21, h - 21);

        g2.setColor(new Color(50, 50, 50));
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        g2.drawString(line1, 40, 70);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 13f));
        int y = 110;
        for (String s : line2.split("\\n")) {
            g2.drawString(s, 40, y);
            y += 18;
        }
        g2.dispose();
        return bi;
    }

}
