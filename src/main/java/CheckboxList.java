import javax.swing.*;
import java.awt.*;

public class CheckboxList extends JPanel {
	JCheckBox[] checkBox;
	public CheckboxList (String[] text){
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

	public boolean[] getSelected(){
		boolean[] selected = new boolean[checkBox.length];
		for(int i=0; i<selected.length; i++){
			selected[i] = checkBox[i].isSelected();
		}
		return selected;
	}
}
