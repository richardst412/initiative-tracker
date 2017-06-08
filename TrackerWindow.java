/* ********************************************************************************************
 * TrackerWindow.java by Tim Richards
 * 
 * An application that keeps track of combat turn order in certain tabletop RPGs.
 * Made with D&D 5th Edition in mind, but can be used and/or modified for other games.
 * 
 * Turn order is displayed as a table, with each item (row) its own creature with a name,
 * an initiative score, an initiative modifier, max/current HP values, and an armor class.
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

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

public class TrackerWindow
{

	protected Shell shell;
	
	// Specifies whether to check or uncheck all items when clicking the name column header
	private boolean checkAll;
	
	// Specify which columns contain which data using constants
	final int NAME = 0;
	final int INIT = 1;
	final int MOD = 2;
	final int HP_CURRENT = 3;
	final int HP_MAX = 4;
	final int AC = 5;

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
		final int SHELL_WIDTH = 1000;
		final int SHELL_HEIGHT = 600;
		
		// Number of columns in the grid layout
		final int NUM_GRID_COLS = 8;
		
		checkAll = true;
		
		shell = new Shell();
		shell.setText("D&D 5E Initiative Tracker");
		
		GridLayout shellLayout = new GridLayout();
		shellLayout.numColumns = NUM_GRID_COLS;
		
		shell.setLayout(shellLayout);
		
		Table table = new Table(shell, SWT.CHECK | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);

		// Creates table with columns initialized
		fillTable(table, NUM_GRID_COLS);
		
		// Creates the buttons that appear along the bottom of the shell
		createButtons(table, NUM_GRID_COLS);
		
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
		


		// Allows table cells to be edited by clicking
		enableEditingOfTable(table);
		
		// Workaround to properly size the table
		// TODO Find a more elegant solution to this
		shell.setSize(SHELL_WIDTH+1, SHELL_HEIGHT);
		shell.setSize(SHELL_WIDTH, SHELL_HEIGHT);
		
		
	}
	
	// Fill empty space by creating blank labels
	private void fillTable(Table table, int gridCols)
	{
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		GridData tableData = new GridData();
		tableData.horizontalAlignment = GridData.FILL;
		tableData.verticalAlignment = GridData.FILL;
		
		tableData.horizontalSpan = gridCols - 2;
		tableData.verticalSpan = 8;
		
		tableData.grabExcessHorizontalSpace = true;
		tableData.grabExcessVerticalSpace = true;
		
		table.setLayoutData(tableData);
		
		initColumns(table);
	}
	
	// Create and label all columns in the table
	private void initColumns(Table table)
	{
		TableColumn column;
		
		column = new TableColumn(table, SWT.NULL);
		column.setText("Name");
		
		column.addListener(SWT.Selection, new Listener()
				{
					public void handleEvent(Event e)
					{
						for (TableItem item : table.getItems())
						{
							item.setChecked(checkAll);
						}
						checkAll = !checkAll;
					}
				});
		
		column = new TableColumn(table, SWT.NULL);
		column.setText("Initiative");
		
		column = new TableColumn(table, SWT.NULL);
		column.setText("Initiave Mod");
		
		column = new TableColumn(table, SWT.NULL);
		column.setText("Current HP");
		
		column = new TableColumn(table, SWT.NULL);
		column.setText("Max HP");
		
		column = new TableColumn(table, SWT.NULL);
		column.setText("Armor Class");
		
	}
	
	// Add new item to the table, prompting user via dialog boxes for creature info
	// Checks that user-entered info is valid numerical value when appropriate
	private void createNewItem(Table table, int numItems)
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
		
		dialog = new NewCreatureDialog(shell, "New Creature", "Enter initiative roll of new creature (or leave blank for rolling later).", null, null);
		do
		{
			if (dialog.open() == Window.CANCEL)
				return;
	
			init = dialog.getCreatureData();
			
			if (!isValidForNumericField(init) && init != "")
				invalidNumberBox.open();
		} while (!isValidForNumericField(init) && init != "");
		
		
		dialog = new NewCreatureDialog(shell, "New Creature", "Enter initiative modifier of new creature.", null, null);
		do
		{
			if (dialog.open() == Window.CANCEL)
				return;
	
			mod = dialog.getCreatureData();
			
			if (!isValidForNumericField(mod))
				invalidNumberBox.open();
		} while (!isValidForNumericField(mod));
		
		dialog = new NewCreatureDialog(shell, "New Creature", "Enter MAXIMUM hitpoints of new creature.", null, null);
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
		
		addItemWithData(table, name, init, mod, HP, AC);
		
		for (int i = 2; i <= numItems; i++)
			addItemWithData(table, name + " " + i, init, mod, HP, AC);

	}
	
	private void addItemWithData(Table table, String newName, String newInit, String newMod, String newHP, String newAC)
	{

		TableItem newItem = new TableItem(table, SWT.NULL);
		newItem.setText(NAME, newName);
		newItem.setText(INIT, newInit);
		newItem.setText(MOD, newMod);
		newItem.setText(HP_CURRENT, newHP);
		newItem.setText(HP_MAX, newHP);
		newItem.setText(AC, newAC);
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
	private void fillEmptySpace(int numLabels, boolean grabHorizontal, boolean grabVertical)
	{
		GridData labelData = new GridData();
		
		labelData.grabExcessHorizontalSpace = grabHorizontal;
		labelData.grabExcessVerticalSpace = grabVertical;
		
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
			
			if (currentItem.getText(1) == "")
				initCurrent = Integer.MIN_VALUE;
			else
				initCurrent = Integer.parseInt(currentItem.getText(1));
			
			if (maxItem.getText(1) == "")
				initMax = Integer.MIN_VALUE;
			else
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
					int quarterWidth = itemBounds.width / 4;
					
					// Set the bounds to the area of the text, not the cell
					GC gc = new GC(Display.getDefault());
					gc.setFont(currentItem.getFont());
					FontMetrics fm = gc.getFontMetrics();
					itemBounds.width = fm.getAverageCharWidth() * currentItem.getText(i).length();
					
					// If the bounds are so small that it's hard to click,
					// make it a bit bigger (25% of the cell width)
					if (itemBounds.width < quarterWidth)
						itemBounds.width = quarterWidth;
					
					// If the point that was clicked is inside the item bounds,
					// currentItem is the item that has been clicked.
					if (itemBounds.contains(pt))
					{
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
	
	private void createUpButton(Table table, int numGridCols)
	{
		GridData upButtonData = new GridData();
		upButtonData.horizontalAlignment = GridData.FILL;
		upButtonData.verticalAlignment = GridData.FILL;
		upButtonData.horizontalSpan = numGridCols / 8;
		upButtonData.verticalSpan = 4;
		upButtonData.grabExcessVerticalSpace = true;
		upButtonData.widthHint = 30;
		
		Button upButton = new Button(shell, SWT.NONE);
		upButton.setText("^");
		upButton.setToolTipText("Shift selected creature one row up");
		upButton.setLayoutData(upButtonData);
		
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
	}
	
	private void createDownButton (Table table, int numGridCols)
	{
Button downButton = new Button(shell, SWT.NONE);
		
		GridData downButtonData = new GridData();
		downButtonData.horizontalAlignment = GridData.FILL;
		downButtonData.verticalAlignment = GridData.FILL;
		downButtonData.horizontalSpan = numGridCols / 8;
		downButtonData.verticalSpan = 4;
		downButtonData.grabExcessVerticalSpace = true;
		downButtonData.widthHint = 30;
		
		downButton.setText("v");
		downButton.setToolTipText("Shift selected creature one row down");
		downButton.setLayoutData(downButtonData);
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
	
	private void createAddButton(Table table, int numGridCols)
	{
		GridData buttonData = new GridData();
		buttonData.horizontalAlignment = GridData.FILL;
		buttonData.horizontalSpan = numGridCols / 8;
		buttonData.widthHint = 200;
		
		Button addButton = new Button(shell, SWT.NONE);
		addButton.setText("Add Creature");
		addButton.setToolTipText("Add new creature to the tracker");
		addButton.setLayoutData(buttonData);
		
		addButton.addListener(SWT.Selection, new Listener()
				{

					@Override
					public void handleEvent(Event e)
					{
						createNewItem(table, 1);
					}
					
				});
	}
	
	// the "Add Creature Clones" button allows the user to add multiple instances of the same creature
	private void createAddMultButton(Table table, int numGridCols)
	{
		GridData buttonData = new GridData();
		buttonData.horizontalAlignment = GridData.FILL;
		buttonData.horizontalSpan = numGridCols / 8;
		buttonData.widthHint = 200;
		
		Button addMultButton = new Button(shell, SWT.NONE);
		addMultButton.setText("Add Creature Copies");
		addMultButton.setToolTipText("Add multiple copies of the same creature with the same stats");
		addMultButton.setLayoutData(buttonData);
		
		addMultButton.addListener(SWT.Selection, new Listener()
				{
					public void handleEvent(Event e)
					{
						NewCreatureDialog dialog = new NewCreatureDialog(shell, "Add Multiple Creatures", 
								"How many versions of this creature do you wish to add?", null, null);
						
						boolean validEntry = false;
						while (!validEntry)
						{
							if (dialog.open() == Window.CANCEL)
								return;
							
							String response = dialog.getCreatureData();
							int responseNum;
							
							if (isValidForNumericField(response) && (responseNum = Integer.parseInt(response)) > 1)
							{
								createNewItem(table, responseNum);
								validEntry = true;
							}
							else
							{
								MessageBox invalidNumberBox = new MessageBox(shell, SWT.OK);
								invalidNumberBox.setText("Invalid number");
								invalidNumberBox.setMessage("Field must contain a numerical value of 2 or greater.");
								invalidNumberBox.open();
							}
						}
					}
				});
	}
	
	private void createRemoveButton(Table table, int numGridCols)
	{
		GridData buttonData = new GridData();
		buttonData.horizontalAlignment = GridData.FILL;
		buttonData.horizontalSpan = numGridCols / 8;
		buttonData.widthHint = 200;
		
		Button removeButton = new Button(shell, SWT.NONE);
		removeButton.setText("Remove Checked Creatures");
		removeButton.setToolTipText("Remove selected creatures from the table");
		removeButton.setLayoutData(buttonData);
		
		removeButton.addListener(SWT.Selection, new Listener()
				{
					public void handleEvent(Event e)
					{
						// get the number of creatures
						ArrayList<TableItem> itemList = new ArrayList<TableItem>();
						
						int numItemsChecked = 0;
						TableItem item;
						
						for (int i = 0; i < table.getItemCount(); i++)
						{
							item = table.getItem(i);
							if (item.getChecked())
							{
								numItemsChecked++;
								itemList.add(item);
							}
						}
						
						if (numItemsChecked == 0)
							return;
						

						// list off all creatures in the "are you sure" menu
						String message = "Are you sure you wish to remove the following " + numItemsChecked + " creatures?";
						int numCreaturesToList = (numItemsChecked > 10) ? 10 : numItemsChecked;
						TableItem checkedItem;
						
						for (int i = 0; i < numCreaturesToList; i++)
						{
							checkedItem = itemList.get(i);
							message += "\n" + checkedItem.getText(0);
						}
						
						if (numItemsChecked > numCreaturesToList)
							message += "\n(... and " + (numItemsChecked - numCreaturesToList) + " others)";
						
						MessageBox confMessageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
						confMessageBox.setText("Remove Creature");
						confMessageBox.setMessage(message);
						
						
						// remove selected creatures if user presses yes
						if (confMessageBox.open() == SWT.YES)
						{
							for (int i = 0; i < table.getItemCount(); i++)
							{
								item = table.getItem(i);
								
								if (item.getChecked())
								{
									table.remove(i--);
								}
							}
						}
					}
				});
	}
	
	private void createSortButton(Table table, int numGridCols)
	{
		GridData buttonData = new GridData();
		buttonData.horizontalAlignment = GridData.FILL;
		buttonData.verticalAlignment = GridData.BEGINNING;
		buttonData.horizontalSpan = numGridCols / 8;
		buttonData.widthHint = 200;
		
		Button sortButton = new Button(shell, SWT.NONE);
		sortButton.setText("Sort");
		sortButton.setToolTipText("Sort creatures by initiative and initiative modifier");
		sortButton.setLayoutData(buttonData);
		
		sortButton.addListener(SWT.Selection, new Listener()
				{

					@Override
					public void handleEvent(Event e)
					{
						sortTableByInitColumn(table);
					}
					
				});
	}
	
	private void createRollButton(Table table, int numGridCols)
	{
		GridData buttonData = new GridData();
		buttonData.horizontalAlignment = GridData.FILL;
		buttonData.verticalAlignment = GridData.END;
		buttonData.horizontalSpan = numGridCols / 8;
		
		Button rollButton = new Button(shell, SWT.NONE);
		rollButton.setText("Roll Initiative!"); 
		rollButton.setToolTipText("Roll Initiative for all \"checked\" creatures");
		rollButton.setLayoutData(buttonData);
		
		rollButton.addListener(SWT.Selection, new Listener()
				{
					public void handleEvent(Event e)
					{
						rollInitiativeForCheckedCreatures(table);
					}
				});
	}
	
	// Creates and adds listeners for all buttons in the shell.
	private void createButtons(Table table, int numGridCols)
	{
		createUpButton(table, numGridCols);
		
		createAddButton(table, numGridCols);
		createAddMultButton(table, numGridCols);
		createRemoveButton(table, numGridCols);
		createSortButton(table, numGridCols);
		
		createDownButton(table, numGridCols);
		
		fillEmptySpace(3, false, false);
		
		createRollButton(table, numGridCols);
	}
	
	// Roll initiative for all creatures that the user checked
	// Generates a number between 1 and 20 and then adds the creature's initiative modifier
	private void rollInitiativeForCheckedCreatures(Table table)
	{
		for (TableItem item : table.getItems())
		{
			if (item.getChecked())
			{
				item.setText(1, "" + ((int)(Math.random() * 20) + 1 + Integer.parseInt(item.getText(2))));
				System.out.println(Integer.parseInt(item.getText(2)));
			}
		}
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