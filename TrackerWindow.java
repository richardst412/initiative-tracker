/* ********************************************************************************************
 * TrackerWindow.java by Tim Richards
 * 
 * An application that keeps track of combat turn order in certain tabletop RPGs.
 * Made with D&D 5th Edition in mind, but can be used and/or modified for other games.
 * 
 * Turn order is displayed as a table, with each item (row) its own creature with a name,
 * an initiative score, an initiative modifier, an HP value, and an armor class.
 * 
 * User can add and remove creatures, sort creatures based on initiative scores (and breaking
 * ties accordingly), and manually sort and move creatures.
 * ******************************************************************************************** */

package tracker;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

public class TrackerWindow
{

	protected Shell shell;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			TrackerWindow window = new TrackerWindow();
			window.open();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents()
	{
		// Initial window size
		final int SHELL_WIDTH = 800;
		final int SHELL_HEIGHT = 600;
		
		// Number of columns in the grid layout
		final int NUM_GRID_COLS = 8;
		
		shell = new Shell();
		shell.setText("D&D 5E Initiative Tracker");
		
		GridLayout shellLayout = new GridLayout();
		shellLayout.numColumns = NUM_GRID_COLS;
		
		shell.setLayout(shellLayout);
		
		Table table = new Table(shell, SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);

		// Creates table with columns initialized
		fillTable(table, NUM_GRID_COLS);
		
		// Resizes columns when shell is resized
		shell.addListener(SWT.Resize, new Listener()
				{
					public void handleEvent(Event e)
					{
						int numCols = table.getColumnCount();
						for (int i = 0; i < numCols; i++)
						{
							table.getColumn(i).setWidth((int)(table.getClientArea().width) / numCols);
						}
					}
				});
		

		shell.setSize(SHELL_WIDTH, SHELL_HEIGHT);

		// Allows table cells to be edited by clicking
		enableEditingOfTable(table);
		
		// Creates the buttons that appear along the bottom of the shell
		createButtons(table, NUM_GRID_COLS);
		
	}
	
	// Fill empty space by creating blank labels
	private void fillTable(Table table, int gridCols)
	{
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		GridData tableData = new GridData();
		tableData.horizontalAlignment = GridData.FILL;
		tableData.verticalAlignment = GridData.FILL;
		
		tableData.horizontalSpan = gridCols;
		
		tableData.grabExcessHorizontalSpace = true;
		tableData.grabExcessVerticalSpace = true;
		
		table.setLayoutData(tableData);
		
		initColumns(table);
	}
	
	// Create and label all columns in the table
	private void initColumns(Table table)
	{
		TableColumn column;
		final int NUM_COLS = 5;
		final int COLUMN_WIDTH = (table.getClientArea().width) / NUM_COLS;
		
		column = new TableColumn(table, SWT.NULL);
		column.setWidth(COLUMN_WIDTH);
		column.setText("Name");
		
		column = new TableColumn(table, SWT.NULL);
		column.setWidth(COLUMN_WIDTH);
		column.setText("Initiative");
		
		column = new TableColumn(table, SWT.NULL);
		column.setWidth(COLUMN_WIDTH);
		column.setText("Initiave Mod");
		
		column = new TableColumn(table, SWT.NULL);
		column.setWidth(COLUMN_WIDTH);
		column.setText("HP");
		
		column = new TableColumn(table, SWT.NULL);
		column.setWidth(COLUMN_WIDTH);
		column.setText("AC");
		
	}
	
	// Add new item to the table, prompting user via dialog boxes for creature info
	// Checks that user-entered info is valid numerical value when appropriate
	private void createNewItem(Table table)
	{
		String name, init, mod, HP, AC;
		NewCreatureDialog dialog;
		
		MessageBox invalidNumberBox = new MessageBox(shell, SWT.OK);
		invalidNumberBox.setText("Invalid number");
		invalidNumberBox.setMessage("This cell must contain a numerical value.");
		
		dialog = new NewCreatureDialog(shell, "New Creature", "Enter name of new creature.", null, null);
		
		if (dialog.open() == Window.CANCEL)
			return;
		
		name = dialog.getCreatureData();
		
		dialog = new NewCreatureDialog(shell, "New Creature", "Enter initiative roll of new creature.", null, null);
		do
		{
			if (dialog.open() == Window.CANCEL)
				return;
	
			init = dialog.getCreatureData();
			
			if (!isValidForNumericField(init))
				invalidNumberBox.open();
		} while (!isValidForNumericField(init));
		
		
		dialog = new NewCreatureDialog(shell, "New Creature", "Enter initiative modifier of new creature.", null, null);
		do
		{
			if (dialog.open() == Window.CANCEL)
				return;
	
			mod = dialog.getCreatureData();
			
			if (!isValidForNumericField(mod))
				invalidNumberBox.open();
		} while (!isValidForNumericField(mod));
		
		dialog = new NewCreatureDialog(shell, "New Creature", "Enter HP of new creature.", null, null);
		do
		{
			if (dialog.open() == Window.CANCEL)
				return;
	
			HP = dialog.getCreatureData();
			
			if (!isValidForNumericField(HP))
				invalidNumberBox.open();
		} while (!isValidForNumericField(HP));
		
		dialog = new NewCreatureDialog(shell, "New Creature", "Enter AC of new creature.", null, null);
		do
		{
			if (dialog.open() == Window.CANCEL)
				return;
	
			AC = dialog.getCreatureData();
			
			if (!isValidForNumericField(AC))
				invalidNumberBox.open();
		} while (!isValidForNumericField(AC));
		

		TableItem newItem = new TableItem(table, SWT.NULL);
		newItem.setText(0, name);
		newItem.setText(1, init);
		newItem.setText(2, mod);
		newItem.setText(3, HP);
		newItem.setText(4, AC);
	}
	
	// Returns true if String s follows the patterns of number of any length,
	// possibly preceded by + or -
	private boolean isValidForNumericField(String s)
	{
		return (s != null && s.matches("[+-]*\\d\\d*"));
	}
	
	// Swaps the data of two TableItems
	private void swapItems(TableItem item1, TableItem item2)
	{
		int numCols = item1.getParent().getColumnCount();
		
		String temp;
		
		for (int i = 0; i < numCols; i++)
		{
			temp = item1.getText(i);
			item1.setText(i, item2.getText(i));
			item2.setText(i, temp);
		}
	}
	
	// Fills empty space with a number of empty labels
	private void fillEmptySpace(int numLabels)
	{
		GridData labelData = new GridData();
		
		labelData.grabExcessHorizontalSpace = true;
		
		for (int i = 0; i < numLabels; i++)
		{
			new Label(shell, SWT.NONE).setLayoutData(labelData);
		}
	}
	
	// Selection Sort of the table based on the initiative column, initiative modifier column,
	// or random numbers, depending on ties.
	private void sortTableByInitColumn(Table table)
	{
		for (int i = 0; i < table.getItemCount(); i++)
		{
			swapItems(table.getItem(i), getMaxInit(table, i));
		}
	}
	
	// Gets the highest initiative score in the table, beginning search at index startingItem.
	// Initiative tie scores are broken by initiative modifier.
	// In the case of an initiative tie and an initiative modifier tie,
	// the max is chosen at random.
	private TableItem getMaxInit(Table table, int startingItem)
	{
		TableItem maxItem = table.getItem(startingItem);
		TableItem currentItem;
		
		int initCurrent, initMax;
		int modCurrent, modMax;
		
		for (int i = startingItem + 1; i < table.getItemCount(); i++)
		{
			currentItem = table.getItem(i);
			
			initCurrent = Integer.parseInt(currentItem.getText(1));
			initMax = Integer.parseInt(maxItem.getText(1));
			
			// init ties are broken by init mod
			if (initCurrent == initMax)
			{
				modCurrent = Integer.parseInt(currentItem.getText(2));
				modMax = Integer.parseInt(maxItem.getText(2));
				
				// init mod ties are broken by random numbers
				if (modCurrent == modMax)
				{
					if (Math.random() > .5)
						maxItem = currentItem;
				}
				else if (modCurrent > modMax)
					maxItem = currentItem;
			}
			else if (initCurrent > initMax)
				maxItem = currentItem;
		}
		
		return maxItem;
	}
	
	// Allows for editing of table cells using the mouse.
	private void enableEditingOfTable(Table table)
	{
		TableEditor editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		
		table.addListener( SWT.MouseDown, event -> {
			
			Control oldEditor = editor.getEditor();
			if (oldEditor != null)
				oldEditor.dispose();
			
			// The point that was clicked
			Point pt = new Point(event.x, event.y);
			
			// The index of the top item that is actually visible, taking scrolling into account
			int index = table.getTopIndex();
			
			while (index < table.getItemCount())
			{
				TableItem currentItem = table.getItem(index);
				for (int i = 0; i < table.getColumnCount(); i++)
				{
					
					
					// The area of the cell currently being checked
					Rectangle itemBounds = currentItem.getBounds(i);
					
					// Set the bounds to the area of the text, not the cell
					GC gc = new GC(Display.getDefault());
					gc.setFont(currentItem.getFont());
					FontMetrics fm = gc.getFontMetrics();
					itemBounds.width = fm.getAverageCharWidth() * currentItem.getText(i).length();
					
					// If the point that was clicked is inside the item bounds,
					// currentItem is the item that has been clicked.
					if (itemBounds.contains(pt))
					{
						System.out.println("Clicked item " + index + " in column " + i);
						
						final int colSelected = i;
						
						// Place an editable text box over the box that was clicked
						// with the same data as the box
						Text newEditor = new Text(table, SWT.NULL);
						newEditor.setText(currentItem.getText(colSelected));
						
						
						newEditor.addModifyListener(me -> {
							Text text = (Text) editor.getEditor();
							editor.getItem().setText(colSelected, text.getText());
						});
						
						if (colSelected != 0)
						{
							newEditor.addFocusListener(new FocusListener()
									{
										String oldText;
								
										@Override
										public void focusGained(FocusEvent arg0)
										{
											oldText = newEditor.getText();
										}
	
										@Override
										public void focusLost(FocusEvent arg0)
										{
											if (!isValidForNumericField(newEditor.getText()))
											{
												MessageBox invalidNumberBox = new MessageBox(shell, SWT.OK);
												invalidNumberBox.setText("Invalid number");
												invalidNumberBox.setMessage("This cell must contain a numerical value.");
												invalidNumberBox.open();
												newEditor.setText(oldText);
											}
										}
								
									});
						}
						
						newEditor.selectAll();
						newEditor.setFocus();
						
						editor.setEditor(newEditor, currentItem, colSelected);
						
						return;
					}
				}
				index++;
			}
		});
	}
	
	// Creates and adds listeners for all buttons in the shell.
	private void createButtons(Table table, int numGridCols)
	{
		GridData buttonData = new GridData();
		buttonData.horizontalAlignment = GridData.FILL;
		buttonData.horizontalSpan = numGridCols / 4;
		buttonData.grabExcessHorizontalSpace = true;
		
		Button addButton = new Button(shell, SWT.NONE);
		addButton.setText("Add Creature");
		addButton.setLayoutData(buttonData);
		
		addButton.addListener(SWT.Selection, new Listener()
				{

					@Override
					public void handleEvent(Event e)
					{
						createNewItem(table);
					}
					
				});
		
		Button removeButton = new Button(shell, SWT.NONE);
		removeButton.setText("Remove Creature");
		removeButton.setLayoutData(buttonData);
		
		removeButton.addListener(SWT.Selection, new Listener()
				{
					public void handleEvent(Event e)
					{
						int selectedIndex = table.getSelectionIndex();
						
						if (selectedIndex >= 0)
						{
							MessageBox confMessageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
							confMessageBox.setText("Remove Creature");
							confMessageBox.setMessage("Are you sure you want to remove "
									+ table.getItem(selectedIndex).getText(0)
									 + " from the initiative tracker?");
							
							if (confMessageBox.open() == SWT.YES)
								table.remove(selectedIndex);
						}
					}
				});
		

		buttonData = new GridData();
		buttonData.horizontalAlignment = GridData.FILL;
		buttonData.horizontalSpan = numGridCols / 8;
		buttonData.grabExcessHorizontalSpace = true;
		
		Button sortButton = new Button(shell, SWT.NONE);
		sortButton.setText("Sort");
		sortButton.setLayoutData(buttonData);
		
		sortButton.addListener(SWT.Selection, new Listener()
				{

					@Override
					public void handleEvent(Event arg0) {
						sortTableByInitColumn(table);
						
					}
					
				});
		
		fillEmptySpace(1);
		
		GridData upButtonData = new GridData();
		upButtonData.horizontalAlignment = GridData.FILL;
		upButtonData.horizontalSpan = numGridCols / 8;
		
		Button upButton = new Button(shell, SWT.NONE);
		Button downButton = new Button(shell, SWT.NONE);
		upButton.setText("Move Up");
		downButton.setText("Move Down");
		
		upButton.setLayoutData(upButtonData);
		downButton.setLayoutData(upButtonData);
		
		upButton.addListener(SWT.Selection, new Listener()
				{
					public void handleEvent(Event e)
					{
						int selectedIndex = table.getSelectionIndex();
						if (selectedIndex < 1)
							return;
						
						swapItems(table.getItem(selectedIndex), table.getItem(selectedIndex - 1));
						table.setSelection(selectedIndex - 1);
					}
				});
		
		downButton.addListener(SWT.Selection, new Listener()
				{
					public void handleEvent(Event e)
					{
						int selectedIndex = table.getSelectionIndex();
						if (selectedIndex < 0 || selectedIndex >= table.getItemCount() - 1)
							return;
						
						swapItems(table.getItem(selectedIndex), table.getItem(selectedIndex + 1));
						table.setSelection(selectedIndex + 1);
					}
				});
	}
}

// Input dialog that allows for saving of user-entered data.
class NewCreatureDialog extends InputDialog
{
	private String inputString;
	
	public NewCreatureDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue,
			IInputValidator validator)
	{
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);

		inputString = "";
		
	}
	
	protected void okPressed()
	{
		inputString = this.getValue();
		super.okPressed();
	}
	
	public String getCreatureData()
	{
		return inputString;
	}
}