package ocaml.editor.actions;

import ocaml.OcamlPlugin;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.editors.text.TextEditor;

public class SelectUpwardOneBlock implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	
	public void run(IAction action) {

		IWorkbenchPage page = window.getActivePage();
		
		if (page == null) {
			OcamlPlugin.logWarning(GotoDefinition.class.getSimpleName() 
					+ ": page is null");
			return;
		}

		IEditorPart editorPart = page.getActiveEditor();
		if (editorPart == null) {
			OcamlPlugin.logError(GotoDefinition.class.getSimpleName() 
					+ ": editorPart is null");
			return;
		}
		
		if (!(editorPart instanceof TextEditor)) {
			OcamlPlugin.logError(GotoDefinition.class.getSimpleName() 
					+ ": only works on ml and mli files");
			return;
		}

		TextEditor editor = (TextEditor) editorPart;
		IEditorInput editorInput = editor.getEditorInput();
		IDocument doc = editor.getDocumentProvider().getDocument(editorInput);
		Control control = (Control)editor.getAdapter(Control.class);

		if (!(control instanceof StyledText)) 
		{
			OcamlPlugin.logError(GotoDefinition.class.getSimpleName() 
					+ ": Cannot get caret position");
			return;
		}
		
		final StyledText styledText = (StyledText) control;
		int cursorOffset = styledText.getCaretOffset();
		
		Point selection = styledText.getSelection();
		int startOffset = (selection.x < cursorOffset) ? selection.x : selection.y;

		try {
			int lineNum= doc.getLineOfOffset(cursorOffset);
			int numOfLine = doc.getNumberOfLines();
			
			if (lineNum <= 0) {
				styledText.setSelection(startOffset, 0);
				return;
			}
			
			int currentLineOffset = doc.getLineOffset(lineNum);
			
			// if previous line is empty and cursor is not in beginning of
			// current block, then go to the beginning.
			int beginOffset = doc.getLineOffset(lineNum-1);
			int endOffset = doc.getLineOffset(lineNum) - 1;
			String prevLine= doc.get(beginOffset, endOffset - beginOffset + 1);
			boolean isPrevLineEmpty = prevLine.trim().isEmpty();
			if (isPrevLineEmpty && (cursorOffset != currentLineOffset)) {
				styledText.setSelection(startOffset, currentLineOffset);
				return;
			}

			// find previous non-empty line which follows an empty line
			lineNum--;
			while (lineNum > 0) {
				beginOffset = doc.getLineOffset(lineNum-1);
				endOffset = doc.getLineOffset(lineNum) - 1;
				prevLine = doc.get(beginOffset, endOffset - beginOffset + 1);
				if (!prevLine.trim().isEmpty()) {
					isPrevLineEmpty = false;
					lineNum--;
				}
				else if (isPrevLineEmpty)
					lineNum--;
				else {
					break;		// stop at this non-empty line
				}
			}
			int newOffset = doc.getLineOffset(lineNum);
			styledText.setSelection(startOffset, newOffset);
		} catch (BadLocationException e) {
			return;
		}
	}
    
	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
}
