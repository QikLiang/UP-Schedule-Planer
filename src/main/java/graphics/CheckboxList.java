package graphics;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class CheckboxList extends JPanel {
	private JCheckBox[] checkBox;
	private String[] text;

	CheckboxList(String[] text){
		this.text = text;
		JPanel list = new JPanel();
		list.setLayout( new BoxLayout(list, BoxLayout.Y_AXIS));
		checkBox = new JCheckBox[text.length];
		for( int i=0; i<text.length; i++ ){
			JCheckBox cb = new JCheckBox(text[i]);
			checkBox[i]=cb;
			list.add(cb);
		}
		JScrollPane scrollBar = new JScrollPane(list);
		scrollBar.setPreferredSize( new Dimension(400, 300) );
		this.add(scrollBar);
	}

	public void setSelected(boolean[] selected){
		if(selected == null){
			return;
		}

		for(int i=0; i<checkBox.length; i++){
			if(selected[i]){
				checkBox[i].setSelected(true);
			}
		}
	}

	boolean[] getSelected(){
		boolean[] selected = new boolean[checkBox.length];
		for(int i=0; i<checkBox.length; i++){
			selected[i] = checkBox[i].isSelected();
		}
		return selected;
	}
}
