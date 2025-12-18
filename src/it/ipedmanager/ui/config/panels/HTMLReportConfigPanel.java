package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;

import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Pannello per la configurazione di HTMLReportConfig.txt
 * Layout ottimizzato con 3 sezioni logiche.
 */
public class HTMLReportConfigPanel extends BaseConfigPanel {

        private PropertiesConfigFile config;

        // Campi di testo - Info Caso
        private JTextField txtHeader;
        private JTextField txtTitle;
        private JTextField txtExaminer;
        private JTextField txtExaminerID;
        private JTextField txtRequester;
        private JTextField txtRequestDoc;
        private JTextField txtInvestigation;
        private JTextField txtReport;
        private JTextField txtMaterial;
        private JTextField txtRecord;
        private JTextField txtRequestDate;
        private JTextField txtReportDate;
        private JTextField txtRecordDate;

        // Campi numerici - Opzioni Report
        private JSpinner spnItemsPerPage;
        private JSpinner spnThumbSize;
        private JSpinner spnVideoStripeWidth;
        private JSpinner spnFramesPerStripe;
        private JSpinner spnThumbsPerPage;

        // Checkbox - Contenuti
        private JCheckBox chkEnableImageThumbs;
        private JCheckBox chkEnableVideoThumbs;
        private JCheckBox chkEnableCategoriesList;
        private JCheckBox chkEnableThumbsGallery;

        public HTMLReportConfigPanel() {
                initComponents();
        }

        private void initComponents() {
                setLayout(new BorderLayout());
                setBackground(BG_COLOR);
                setBorder(new EmptyBorder(8, 10, 8, 10));

                JPanel content = new JPanel();
                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                content.setBackground(BG_COLOR);

                // === SEZIONE 1: INTESTAZIONE REPORT ===
                content.add(createHeaderSection());
                content.add(Box.createVerticalStrut(10));

                // === SEZIONE 2: DATI CASO ===
                content.add(createCaseInfoSection());
                content.add(Box.createVerticalStrut(10));

                // === SEZIONE 3: DATE ===
                content.add(createDatesSection());
                content.add(Box.createVerticalStrut(10));

                // === SEZIONE 4: CONTENUTI & PAGINAZIONE ===
                content.add(createContentSection());

                content.add(Box.createVerticalGlue());

                JScrollPane scrollPane = new JScrollPane(content);
                scrollPane.setBorder(null);
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                add(scrollPane, BorderLayout.CENTER);
        }

        private JPanel createHeaderSection() {
                JPanel panel = createCompactSectionPanel(BundleManager.getString("panel.report.section.header"));

                txtHeader = createPlaceholderTextField(BundleManager.getString("panel.report.placeholder.header"));
                txtTitle = createPlaceholderTextField(BundleManager.getString("panel.report.placeholder.title"));

                JPanel row = new JPanel(new GridBagLayout());
                row.setBackground(BG_COLOR);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(2, 4, 2, 8);
                gbc.anchor = GridBagConstraints.WEST;

                gbc.gridx = 0;
                gbc.weightx = 0;
                row.add(createStyledLabel(BundleManager.getString("panel.report.label.header")), gbc);
                gbc.gridx = 1;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                row.add(txtHeader, gbc);

                gbc.gridx = 2;
                gbc.weightx = 0;
                gbc.fill = GridBagConstraints.NONE;
                row.add(createStyledLabel(BundleManager.getString("panel.report.label.title")), gbc);
                gbc.gridx = 3;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                row.add(txtTitle, gbc);

                panel.add(row, BorderLayout.CENTER);
                return panel;
        }

        private JPanel createCaseInfoSection() {
                JPanel panel = createCompactSectionPanel(BundleManager.getString("panel.report.section.caseInfo"));

                JPanel grid = new JPanel(new GridBagLayout());
                grid.setBackground(BG_COLOR);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(2, 4, 2, 8);
                gbc.anchor = GridBagConstraints.WEST;

                // Row 0: Esaminatore | ID
                txtExaminer = createPlaceholderTextField(BundleManager.getString("panel.report.placeholder.examiner"));
                txtExaminerID = createPlaceholderTextField(
                                BundleManager.getString("panel.report.placeholder.examinerId"));
                addLabelFieldPair(grid, gbc, 0, BundleManager.getString("panel.report.label.examiner"), txtExaminer,
                                BundleManager.getString("panel.report.label.examinerId"), txtExaminerID);

                // Row 1: Richiedente | Doc
                txtRequester = createPlaceholderTextField(
                                BundleManager.getString("panel.report.placeholder.requester"));
                txtRequestDoc = createPlaceholderTextField(BundleManager.getString("panel.report.placeholder.doc"));
                addLabelFieldPair(grid, gbc, 1, BundleManager.getString("panel.report.label.requester"), txtRequester,
                                BundleManager.getString("panel.report.label.doc"), txtRequestDoc);

                // Row 2: Indagine | N° Esame
                txtInvestigation = createPlaceholderTextField(
                                BundleManager.getString("panel.report.placeholder.investigation"));
                txtReport = createPlaceholderTextField(BundleManager.getString("panel.report.placeholder.report"));
                addLabelFieldPair(grid, gbc, 2, BundleManager.getString("panel.report.label.investigation"),
                                txtInvestigation, BundleManager.getString("panel.report.label.examNo"), txtReport);

                // Row 3: Materiale | Record
                txtMaterial = createPlaceholderTextField(BundleManager.getString("panel.report.placeholder.material"));
                txtRecord = createPlaceholderTextField(BundleManager.getString("panel.report.placeholder.record"));
                addLabelFieldPair(grid, gbc, 3, BundleManager.getString("panel.report.label.material"), txtMaterial,
                                BundleManager.getString("panel.report.label.record"), txtRecord);

                panel.add(grid, BorderLayout.CENTER);
                return panel;
        }

        private JPanel createDatesSection() {
                JPanel panel = createCompactSectionPanel(BundleManager.getString("panel.report.section.dates"));

                txtRequestDate = createPlaceholderTextField(BundleManager.getString("panel.report.placeholder.date"));
                txtReportDate = createPlaceholderTextField(BundleManager.getString("panel.report.placeholder.date"));
                txtRecordDate = createPlaceholderTextField(BundleManager.getString("panel.report.placeholder.date"));

                JPanel row = new JPanel(new GridBagLayout());
                row.setBackground(BG_COLOR);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(2, 4, 2, 8);
                gbc.anchor = GridBagConstraints.WEST;

                gbc.gridx = 0;
                gbc.weightx = 0;
                row.add(createStyledLabel(BundleManager.getString("panel.report.label.request")), gbc);
                gbc.gridx = 1;
                gbc.weightx = 0.33;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                row.add(txtRequestDate, gbc);

                gbc.gridx = 2;
                gbc.weightx = 0;
                gbc.fill = GridBagConstraints.NONE;
                row.add(createStyledLabel(BundleManager.getString("panel.report.label.report")), gbc);
                gbc.gridx = 3;
                gbc.weightx = 0.33;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                row.add(txtReportDate, gbc);

                gbc.gridx = 4;
                gbc.weightx = 0;
                gbc.fill = GridBagConstraints.NONE;
                row.add(createStyledLabel(BundleManager.getString("panel.report.label.registration")), gbc);
                gbc.gridx = 5;
                gbc.weightx = 0.33;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                row.add(txtRecordDate, gbc);

                panel.add(row, BorderLayout.CENTER);
                return panel;
        }

        private JPanel createContentSection() {
                JPanel panel = createCompactSectionPanel(BundleManager.getString("panel.report.section.content"));

                JPanel grid = new JPanel(new GridBagLayout());
                grid.setBackground(BG_COLOR);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(4, 4, 4, 4);
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.HORIZONTAL;

                // === ROW 0: TUTTI I FLAG (checkbox) - espansi su tutta la larghezza ===
                chkEnableImageThumbs = createStyledCheckBox(BundleManager.getString("panel.report.check.imageThumbs"));
                chkEnableVideoThumbs = createStyledCheckBox(BundleManager.getString("panel.report.check.videoThumbs"));
                chkEnableCategoriesList = createStyledCheckBox(
                                BundleManager.getString("panel.report.check.categories"));
                chkEnableThumbsGallery = createStyledCheckBox(
                                BundleManager.getString("panel.report.check.thumbsGallery"));

                gbc.gridy = 0;
                gbc.gridx = 0;
                gbc.weightx = 0.25;
                grid.add(chkEnableImageThumbs, gbc);
                gbc.gridx = 1;
                gbc.weightx = 0.25;
                grid.add(chkEnableVideoThumbs, gbc);
                gbc.gridx = 2;
                gbc.weightx = 0.25;
                grid.add(chkEnableCategoriesList, gbc);
                gbc.gridx = 3;
                gbc.weightx = 0.25;
                grid.add(chkEnableThumbsGallery, gbc);

                // === SPINNER CREATION ===
                spnThumbSize = createStyledSpinner();
                spnThumbSize.setModel(new SpinnerNumberModel(112, 50, 500, 8));
                spnThumbSize.setToolTipText(BundleManager.getString("panel.report.tooltip.thumbSize"));

                spnVideoStripeWidth = createStyledSpinner();
                spnVideoStripeWidth.setModel(new SpinnerNumberModel(800, 200, 1600, 100));
                spnVideoStripeWidth.setToolTipText(BundleManager.getString("panel.report.tooltip.videoWidth"));

                spnFramesPerStripe = createStyledSpinner();
                spnFramesPerStripe.setModel(new SpinnerNumberModel(8, 1, 20, 1));
                spnFramesPerStripe.setToolTipText(BundleManager.getString("panel.report.tooltip.frames"));

                spnItemsPerPage = createStyledSpinner();
                spnItemsPerPage.setModel(new SpinnerNumberModel(100, 10, 1000, 10));
                spnItemsPerPage.setToolTipText(BundleManager.getString("panel.report.tooltip.itemsPage"));

                spnThumbsPerPage = createStyledSpinner();
                spnThumbsPerPage.setModel(new SpinnerNumberModel(500, 50, 2000, 50));
                spnThumbsPerPage.setToolTipText(BundleManager.getString("panel.report.tooltip.thumbsPage"));

                // === ROW 1: Prima riga di valori ===
                gbc.gridy = 1;
                gbc.gridx = 0;
                gbc.weightx = 0.15;
                gbc.fill = GridBagConstraints.NONE;
                grid.add(createStyledLabel(BundleManager.getString("panel.report.label.imgSize")), gbc);
                gbc.gridx = 1;
                gbc.weightx = 0.35;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                grid.add(spnThumbSize, gbc);
                gbc.gridx = 2;
                gbc.weightx = 0.15;
                gbc.fill = GridBagConstraints.NONE;
                grid.add(createStyledLabel(BundleManager.getString("panel.report.label.videoWidth")), gbc);
                gbc.gridx = 3;
                gbc.weightx = 0.35;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                grid.add(spnVideoStripeWidth, gbc);

                // === ROW 2: Seconda riga di valori ===
                gbc.gridy = 2;
                gbc.gridx = 0;
                gbc.weightx = 0.15;
                gbc.fill = GridBagConstraints.NONE;
                grid.add(createStyledLabel(BundleManager.getString("panel.report.label.frames")), gbc);
                gbc.gridx = 1;
                gbc.weightx = 0.35;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                grid.add(spnFramesPerStripe, gbc);
                gbc.gridx = 2;
                gbc.weightx = 0.15;
                gbc.fill = GridBagConstraints.NONE;
                grid.add(createStyledLabel(BundleManager.getString("panel.report.label.itemsPage")), gbc);
                gbc.gridx = 3;
                gbc.weightx = 0.35;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                grid.add(spnItemsPerPage, gbc);

                // === ROW 3: Terza riga ===
                gbc.gridy = 3;
                gbc.gridx = 0;
                gbc.weightx = 0.15;
                gbc.fill = GridBagConstraints.NONE;
                grid.add(createStyledLabel(BundleManager.getString("panel.report.label.thumbsPage")), gbc);
                gbc.gridx = 1;
                gbc.weightx = 0.35;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                grid.add(spnThumbsPerPage, gbc);

                panel.add(grid, BorderLayout.CENTER);
                return panel;
        }

        private void addLabelFieldPair(JPanel grid, GridBagConstraints gbc, int row,
                        String label1, JTextField field1, String label2, JTextField field2) {
                gbc.gridy = row;

                gbc.gridx = 0;
                gbc.weightx = 0;
                gbc.fill = GridBagConstraints.NONE;
                grid.add(createStyledLabel(label1), gbc);
                gbc.gridx = 1;
                gbc.weightx = 0.5;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                grid.add(field1, gbc);

                gbc.gridx = 2;
                gbc.weightx = 0;
                gbc.fill = GridBagConstraints.NONE;
                grid.add(createStyledLabel(label2), gbc);
                gbc.gridx = 3;
                gbc.weightx = 0.5;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                grid.add(field2, gbc);
        }

        private JPanel createCompactSectionPanel(String title) {
                JPanel panel = new JPanel(new BorderLayout(0, 4));
                panel.setBackground(BG_COLOR);
                panel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                                new EmptyBorder(4, 0, 6, 0)));

                JLabel titleLabel = new JLabel(title.toUpperCase());
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                titleLabel.setForeground(ACCENT_COLOR);
                panel.add(titleLabel, BorderLayout.NORTH);

                return panel;
        }

        public void setConfig(PropertiesConfigFile config) {
                this.config = config;
                loadConfig();
        }

        @Override
        public void loadConfig() {
                if (config == null)
                        return;

                // Text fields: NON carichiamo i valori dal config per mostrare i placeholder
                // I valori verranno caricati solo quando l'utente salva
                // (i campi mostrano testo di esempio grigio che scompare al click)

                // Spinners
                spnItemsPerPage.setValue(config.getInt("ItemsPerPage", 100));
                spnThumbSize.setValue(config.getInt("ThumbSize", 112));
                spnVideoStripeWidth.setValue(config.getInt("VideoStripeWidth", 800));
                spnFramesPerStripe.setValue(config.getInt("FramesPerStripe", 8));
                spnThumbsPerPage.setValue(config.getInt("ThumbsPerPage", 500));

                // Checkboxes
                chkEnableImageThumbs.setSelected(config.getBoolean("EnableImageThumbs", true));
                chkEnableVideoThumbs.setSelected(config.getBoolean("EnableVideoThumbs", true));
                chkEnableCategoriesList.setSelected(config.getBoolean("EnableCategoriesList", true));
                chkEnableThumbsGallery.setSelected(config.getBoolean("EnableThumbsGallery", true));
        }

        @Override
        public void saveConfig() {
                if (config == null)
                        return;

                // Text fields
                config.setString("Header", txtHeader.getText());
                config.setString("Title", txtTitle.getText());
                config.setString("Examiner", txtExaminer.getText());
                config.setString("ExaminerID", txtExaminerID.getText());
                config.setString("Requester", txtRequester.getText());
                config.setString("RequestDoc", txtRequestDoc.getText());
                config.setString("Investigation", txtInvestigation.getText());
                config.setString("Report", txtReport.getText());
                config.setString("Material", txtMaterial.getText());
                config.setString("Record", txtRecord.getText());
                config.setString("RequestDate", txtRequestDate.getText());
                config.setString("ReportDate", txtReportDate.getText());
                config.setString("RecordDate", txtRecordDate.getText());

                // Spinners
                config.setInt("ItemsPerPage", (Integer) spnItemsPerPage.getValue());
                config.setInt("ThumbSize", (Integer) spnThumbSize.getValue());
                config.setInt("VideoStripeWidth", (Integer) spnVideoStripeWidth.getValue());
                config.setInt("FramesPerStripe", (Integer) spnFramesPerStripe.getValue());
                config.setInt("ThumbsPerPage", (Integer) spnThumbsPerPage.getValue());

                // Checkboxes
                config.setBoolean("EnableImageThumbs", chkEnableImageThumbs.isSelected());
                config.setBoolean("EnableVideoThumbs", chkEnableVideoThumbs.isSelected());
                config.setBoolean("EnableCategoriesList", chkEnableCategoriesList.isSelected());
                config.setBoolean("EnableThumbsGallery", chkEnableThumbsGallery.isSelected());
        }
}
