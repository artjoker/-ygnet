package com.cygnet.ourdrive.swingTail;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class FilterDialog extends JDialog implements ActionListener, ListSelectionListener{

	private static final Map<String, Color> COLOR_MAP;
	private static final String DEFAULT_SOUND = "/swingtail/Blip.wav";

	static {
		COLOR_MAP = new HashMap<String, Color>();
		Field[] fields = Color.class.getDeclaredFields();

		for (Field field : fields) {

			if (field.getType() == Color.class) {

				if (Modifier.isStatic(field.getModifiers())) {
					try {
						Color color = (Color) field.get(null);
						String name = field.getName();
						COLOR_MAP.put(name.toLowerCase(),color);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static final long serialVersionUID = 7965405256271997370L;
	private final SettingHandler settingHandler;

	private JPanel mainPanel = new JPanel();
	private JPanel soundPanel = new JPanel();

	private final JLabel triggerLabel 		= new JLabel("Trigger");
	private JTextField triggerTextField		= new JTextField(20);
	private JRadioButton startsRadioButton	= new JRadioButton("Starts with");
	private JRadioButton containsRadioButton= new JRadioButton("Contains");
	private JRadioButton endsRadioButton	= new JRadioButton("Ends with");
	private ButtonGroup buttonGroup			= new ButtonGroup();
	private JCheckBox markAsBoldCheckBox	= new JCheckBox("Mark text as bold");
	private JCheckBox markWithColorCheckBox	= new JCheckBox("Mark text with color");
	private JCheckBox soundWarningCheckBox	= new JCheckBox("Sound warning");
	private JCheckBox hideCheckBox			= new JCheckBox("Hide");
	private JCheckBox pauseCheckBox			= new JCheckBox("Pause when found");
	private JCheckBox activeCheckBox		= new JCheckBox("Activate filter");
	private JCheckBox defaultSoundCheckBox	= new JCheckBox("Default sound");
	private JTextArea soundTextArea			= new JTextArea(2,30);
	private JButton selectSoundFileButton	= new JButton("Select Sound File");
	private JComboBox colorComboBox			= new JComboBox(COLOR_MAP.keySet().toArray());
	private DefaultTableModel model 		= new DefaultTableModel();
	private JTable filterList				= new JTable(model);
	private JButton saveButton				= new JButton("Save");
	private JButton removeButton			= new JButton("Remove selected filter");
	private JScrollPane scrollPane			= new JScrollPane(filterList);
	private JFileChooser soundFileChooser;
	private FileFilter wavFilter = new FileNameExtensionFilter("WAW file", "wav", "WAV");


	public FilterDialog (SettingHandler settingHandler, JTabbedPane pane){
		soundPanel.setBorder(new TitledBorder("Sound"));
		soundWarningCheckBox.addActionListener(this);
		soundTextArea.setLineWrap(true);
		soundTextArea.setWrapStyleWord(true);
		soundTextArea.setEditable(false);
		selectSoundFileButton.addActionListener(this);
		saveButton.addActionListener(this);
		removeButton.addActionListener(this);
		removeButton.setVisible(false);
		hideCheckBox.addActionListener(this);
		defaultSoundCheckBox.addActionListener(this);
		filterList.getSelectionModel().addListSelectionListener(this);
		buttonGroup.add(startsRadioButton);
		startsRadioButton.setSelected(true);
		buttonGroup.add(containsRadioButton);
		buttonGroup.add(endsRadioButton);
		colorComboBox.setSelectedItem("RED");
		model.addColumn("Filters");
		for (Filter filter: settingHandler.getFilters()){
			model.addRow(new Object[]{filter.getTriggerValue()});
		}
		this.settingHandler = settingHandler;
		//this.pane = pane;

		GroupLayout layout = new GroupLayout(mainPanel);

		mainPanel.setLayout(layout);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, true)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
										.addComponent(triggerLabel)
										.addComponent(startsRadioButton)
										.addComponent(markAsBoldCheckBox)
										.addComponent(markWithColorCheckBox)
										.addComponent(soundWarningCheckBox)
										.addComponent(hideCheckBox)
										.addComponent(pauseCheckBox)
										.addComponent(activeCheckBox))
										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
												.addComponent(triggerTextField)
												.addComponent(containsRadioButton)
												.addComponent(colorComboBox)
												.addComponent(soundPanel)
												.addComponent(removeButton))
												.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
														.addComponent(endsRadioButton)
														.addComponent(saveButton)))
														.addComponent(scrollPane))

		);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, true)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
										.addComponent(triggerLabel)
										.addComponent(triggerTextField))
										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
												.addComponent(startsRadioButton)
												.addComponent(containsRadioButton)
												.addComponent(endsRadioButton))
												.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
														.addComponent(markAsBoldCheckBox))
														.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
																.addComponent(markWithColorCheckBox)
																.addComponent(colorComboBox))
																.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
																		.addComponent(soundWarningCheckBox)
																		.addComponent(soundPanel))
																		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
																				.addComponent(hideCheckBox))
																				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
																						.addComponent(pauseCheckBox))
																						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
																								.addComponent(activeCheckBox))
																								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, true)
																										.addComponent(saveButton)
																										.addComponent(removeButton))))
																										.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, true)
																												.addComponent(scrollPane))
		);
		this.add(mainPanel);

		GroupLayout soundPanellayout = new GroupLayout(soundPanel);

		soundPanel.setLayout(soundPanellayout);

		soundPanellayout.setHorizontalGroup(soundPanellayout.createSequentialGroup()
				.addGroup(soundPanellayout.createParallelGroup(GroupLayout.Alignment.LEADING, true)
						.addGroup(soundPanellayout.createParallelGroup(GroupLayout.Alignment.LEADING, true)
								.addComponent(soundTextArea)
						)
						.addGroup(soundPanellayout.createSequentialGroup()
								.addComponent(defaultSoundCheckBox)
								.addComponent(selectSoundFileButton)
						)

				));

		soundPanellayout.setVerticalGroup(soundPanellayout.createSequentialGroup()
				.addGroup(soundPanellayout.createParallelGroup(GroupLayout.Alignment.BASELINE, true)
						.addComponent(soundTextArea)
				)
				.addGroup(soundPanellayout.createParallelGroup(GroupLayout.Alignment.TRAILING, true)
						.addGroup(soundPanellayout.createParallelGroup(GroupLayout.Alignment.LEADING, true)
								.addComponent(defaultSoundCheckBox)
						)
						.addGroup(soundPanellayout.createParallelGroup(GroupLayout.Alignment.TRAILING, true)
								.addComponent(selectSoundFileButton)
						)
				));

		Point point = getLocation();
		point.x += 100;
		point.y += 100;
		this.setLocation(point);
		this.setPreferredSize(new Dimension(470, 400));
		this.setSize(new Dimension(470, 400));
		this.setResizable(false);
		this.setVisible(true);
		this.setAlwaysOnTop(true);
		this.setupPage(false);
	}

	private void setupPage (boolean isHide){
		if (isHide){
			this.markAsBoldCheckBox.setSelected(false);
			this.markWithColorCheckBox.setSelected(false);
			this.colorComboBox.setSelectedItem(false);
			this.soundWarningCheckBox.setSelected(false);
			this.soundTextArea.setText("");
			this.defaultSoundCheckBox.setSelected(false);
			this.pauseCheckBox.setSelected(false);

			this.markAsBoldCheckBox.setEnabled(false);
			this.markWithColorCheckBox.setEnabled(false);
			this.colorComboBox.setEnabled(false);
			this.soundWarningCheckBox.setEnabled(false);
			this.soundTextArea.setBackground(this.getBackground());
			this.defaultSoundCheckBox.setEnabled(false);
			this.selectSoundFileButton.setEnabled(false);
			this.pauseCheckBox.setEnabled(false);
		}else{
			this.markAsBoldCheckBox.setSelected(false);
			this.markWithColorCheckBox.setSelected(false);
			this.colorComboBox.setSelectedItem(false);
			this.soundWarningCheckBox.setSelected(false);
			this.soundTextArea.setText("");
			this.soundTextArea.setBackground(this.getBackground());
			this.defaultSoundCheckBox.setSelected(false);
			this.hideCheckBox.setSelected(false);
			this.pauseCheckBox.setSelected(false);
			this.activeCheckBox.setSelected(false);

			this.markAsBoldCheckBox.setEnabled(true);
			this.markWithColorCheckBox.setEnabled(true);
			this.colorComboBox.setEnabled(true);
			this.soundWarningCheckBox.setEnabled(true);
			this.defaultSoundCheckBox.setEnabled(false);
			this.selectSoundFileButton.setEnabled(false);
			this.hideCheckBox.setEnabled(true);
			this.pauseCheckBox.setEnabled(true);
			this.activeCheckBox.setEnabled(true);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(soundWarningCheckBox)){
			if (soundWarningCheckBox.isSelected()){
				this.soundTextArea.setText("");
				this.soundTextArea.setBackground(Color.WHITE);
				this.defaultSoundCheckBox.setEnabled(true);
				this.defaultSoundCheckBox.setSelected(false);
				this.selectSoundFileButton.setEnabled(true);
			}else{
				this.soundTextArea.setText("");
				this.soundTextArea.setBackground(this.getBackground());
				this.defaultSoundCheckBox.setEnabled(false);
				this.defaultSoundCheckBox.setSelected(false);
				this.selectSoundFileButton.setEnabled(false);
			}
		}else if (e.getSource().equals(defaultSoundCheckBox)){
			if (defaultSoundCheckBox.isSelected()){
				soundTextArea.setText("");
				soundTextArea.setBackground(this.getBackground());
				soundTextArea.setEnabled(false);
				selectSoundFileButton.setEnabled(false);
			}else{
				soundTextArea.setText("");
				soundTextArea.setEnabled(true);
				soundTextArea.setBackground(Color.WHITE);
				selectSoundFileButton.setEnabled(true);
			}
		}else if (e.getActionCommand().equalsIgnoreCase("Hide")){
			this.setupPage(hideCheckBox.isSelected());
		}else if (e.getActionCommand().equalsIgnoreCase("Save")){

			for (int i=0; i<settingHandler.getFilters().size(); i++){
				Filter filter = settingHandler.getFilters().get(i);
				if (filter.getTriggerValue().equals(triggerTextField.getText())){
					settingHandler.getFilters().remove(i);
					model.removeRow(i);
				}
			}

			if (triggerTextField.getText().equalsIgnoreCase("")){
				JOptionPane.showMessageDialog(this, "You can not save without trigger value.", "Error saving filter", JOptionPane.ERROR_MESSAGE);
			}else if ((soundWarningCheckBox.isSelected())&& (soundTextArea.getText().equalsIgnoreCase("") && !defaultSoundCheckBox.isSelected())){
				JOptionPane.showMessageDialog(this, "You can not save without selecting a sound file or \"Default sound\".", "Error saving filter", JOptionPane.ERROR_MESSAGE);
			}else{
				Filter filter = new Filter();
				filter.setTriggerValue(triggerTextField.getText());
				filter.setStartsWith(startsRadioButton.isSelected());
				filter.setContains(containsRadioButton.isSelected());
				filter.setEndsWith(endsRadioButton.isSelected());
				filter.setMarkAsBold(markAsBoldCheckBox.isSelected());
				filter.setMarkWithColor(markWithColorCheckBox.isSelected());
				filter.setColor(COLOR_MAP.get(colorComboBox.getSelectedItem()));
				filter.setSoundWarning(soundWarningCheckBox.isSelected());
				if (defaultSoundCheckBox.isSelected()){

					filter.setSoundFilePath(DEFAULT_SOUND);
				}else{
					filter.setSoundFilePath(soundTextArea.getText());
				}
				filter.setHide(this.hideCheckBox.isSelected());
				filter.setFilterActive(this.activeCheckBox.isSelected());
				filter.setPauseOnFind(this.pauseCheckBox.isSelected());

				filter.loadSoundFile();
				
				settingHandler.addFilters(filter);
				settingHandler.saveSettings();
				model.addRow(new Object[]{filter.getTriggerValue()});
			}
		}else if (e.getActionCommand().equalsIgnoreCase("Remove selected filter")){
			for (int row: filterList.getSelectedRows()){
				if (JOptionPane.showOptionDialog(this,
						"Are you sure you want to remove the selected filter",
						"Remove filter",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE,
						null,
						new String[]{"Yes","No"},
				"No") == 0){
					// User selected yes, remove the filter and reset all the values.
					settingHandler.getFilters().remove(row);
					model.removeRow(row);
					this.setupPage(false);
				}
			}
		}else if(e.getSource().equals(selectSoundFileButton)){
			soundFileChooser = new JFileChooser();
			soundFileChooser.setAcceptAllFileFilterUsed(false);
			soundFileChooser.setFileFilter(wavFilter);
			int result = soundFileChooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				try {
					soundTextArea.setText(soundFileChooser.getSelectedFile().getCanonicalPath());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()){
			// filter out cause we don't want 2 actions being made just cause a click
		}else{
			removeButton.setVisible(true);
			for (int row: filterList.getSelectedRows()){
				Filter filter = settingHandler.getFilters().get(row);

				this.setupPage(filter.isHide());
				this.triggerTextField.setText(filter.getTriggerValue());
				this.startsRadioButton.setSelected(filter.isStartsWith());
				this.containsRadioButton.setSelected(filter.isContains());
				this.endsRadioButton.setSelected(filter.isEndsWith());
				this.markAsBoldCheckBox.setSelected(filter.isMarkAsBold());
				this.markWithColorCheckBox.setSelected(filter.isMarkWithColor());
				for(Entry<String,Color> colorEntry : COLOR_MAP.entrySet()){

					if(filter.getColor().equals(colorEntry.getValue())){
						this.colorComboBox.setSelectedItem(colorEntry.getKey());
						break;
					}
				}
				if (filter.isSoundWarning()){
					this.soundWarningCheckBox.setSelected(true);
					if (filter.getSoundFilePath().equalsIgnoreCase(DEFAULT_SOUND)){
						this.soundTextArea.setText("");
						this.soundTextArea.setBackground(this.getBackground());
						this.defaultSoundCheckBox.setEnabled(true);
						this.defaultSoundCheckBox.setSelected(true);
						this.selectSoundFileButton.setEnabled(false);
					}else{
						this.soundTextArea.setText(filter.getSoundFilePath());
						this.soundTextArea.setBackground(this.getBackground());
						this.defaultSoundCheckBox.setEnabled(true);
						this.defaultSoundCheckBox.setSelected(false);
						this.selectSoundFileButton.setEnabled(true);
					}
				}else{
					this.soundWarningCheckBox.setSelected(false);
					this.soundTextArea.setText("");
					this.soundTextArea.setBackground(this.getBackground());
					this.defaultSoundCheckBox.setSelected(false);
					this.defaultSoundCheckBox.setEnabled(false);
					this.selectSoundFileButton.setEnabled(false);
				}
				this.hideCheckBox.setSelected(filter.isHide());
				this.pauseCheckBox.setSelected(filter.isPauseOnFind());
				this.activeCheckBox.setSelected(filter.isFilterActive());
			}
		}
	}

}
