package graphics;

import javax.swing.*;
import java.awt.*;

class CheckboxList extends JPanel {
	private JCheckBox[] checkBox;

	CheckboxList(String[] text){
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

	boolean[] getSelected(){
		boolean[] selected = new boolean[checkBox.length];
		for(int i=0; i<selected.length; i++){
			selected[i] = checkBox[i].isSelected();
		}
		return selected;
	}
}
