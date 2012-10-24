package greeter.client;

import greeter.api.GreeterInterface;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class GreeterClient implements BundleActivator {

	private JFrame frame = null;
	
	@Override
	public void start(final BundleContext context) throws Exception {
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				createAndShowGUI(context);
			}
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		synchronized(this){
			if(frame!=null){
				frame.setVisible(false);
				frame.dispose();
				frame = null;
			}
		}
	}

	private void createAndShowGUI(final BundleContext context){
		synchronized(this){
			if(frame==null){
				frame = new JFrame("Greeter test");
			
				frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				frame.setSize(300,300);
			
				final JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				final JTextField text = new JTextField("< enter name >");
				text.setAlignmentX(Component.CENTER_ALIGNMENT);
				panel.add(text);
				final JButton button = new JButton("Send greetings!");
				button.setAlignmentX(Component.CENTER_ALIGNMENT);
				panel.add(button);
				final JLabel label = new JLabel("");
				label.setAlignmentX(Component.CENTER_ALIGNMENT);
				panel.add(label);
				frame.add(panel);
			
				
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						ServiceReference ref = context.getServiceReference(GreeterInterface.class.getName());
						if(ref!=null){
							GreeterInterface greeter = (GreeterInterface)context.getService(ref);
							label.setText(greeter.greet(text.getText()));
							context.ungetService(ref);
						} else {
							label.setText("No greeter service available");
						}
						frame.repaint();
					}
				});
			} 
			
			frame.pack();
			frame.setVisible(true);
		}
	}
}
