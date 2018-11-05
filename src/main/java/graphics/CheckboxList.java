package graphics;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

class CheckboxList extends JPanel {
	private ArrayList<JCheckBox> checkBox;
	private Collection<String> text;

	CheckboxList(Collection<String> text){
		this.text = text;
		JPanel list = new JPanel();
		list.setLayout( new BoxLayout(list, BoxLayout.Y_AXIS));
		checkBox = new ArrayList<>(text.size());
		for(String line : text){
			JCheckBox cb = new JCheckBox(line);
			checkBox.add(cb);
			list.add(cb);
		}
		JScrollPane scrollBar = new JScrollPane(list);
		// make the scroll bar move faster
		scrollBar.getVerticalScrollBar().setUnitIncrement(16);
		scrollBar.setPreferredSize( new Dimension(400, 300) );
		this.add(scrollBar);
	}

	public void setSelected(Set<String> selected){
		if(selected == null){
			return;
		}

		checkBox.stream().filter(selected::contains).forEach(cb -> cb.setSelected(true));
	}

	Set<String> getSelected(){
		return checkBox.stream().filter(JCheckBox::isSelected)
				.map(JCheckBox::getText).collect(Collectors.toSet());
	}
}
