/*
 * Copyright (C) 2013-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Jelena Barth
 * Copyright (C) 2015 University of Freiburg
 * Copyright (C) 2009-2015 pashko
 * 
 * This file is part of the ULTIMATE JungVisualization plug-in.
 * 
 * The ULTIMATE JungVisualization plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE JungVisualization plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE JungVisualization plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE JungVisualization plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE JungVisualization plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.output.jungvisualization.editor;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JPanel;

import de.uni_freiburg.informatik.ultimate.model.structure.VisualizationEdge;
import de.uni_freiburg.informatik.ultimate.model.structure.VisualizationNode;
import de.uni_freiburg.informatik.ultimate.plugins.output.jungvisualization.actions.MenuActions.Mode;
import de.uni_freiburg.informatik.ultimate.plugins.output.jungvisualization.graph.GraphListener;
import de.uni_freiburg.informatik.ultimate.plugins.output.jungvisualization.selection.JungSelectionProvider;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;

/**
 * Defines an editor for the JungVisualization plug-in.
 * 
 * @see {@link EditorPart}
 * @see {@link IPartListener}
 * @author lena
 */
public class JungEditor extends EditorPart implements IPartListener {

	public static final String ID = "de.uni_freiburg.informatik.ultimate.plugins.output.jungvisualization.editor.JungEditor";
	public static final String VV_ID_EDITOR_PROPERTY_KEY = "VisualizationViewerID";

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(((JungEditorInput) input).getName());
		site.getWorkbenchWindow().getPartService().addPartListener(this);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		JungEditorInput ei = (JungEditorInput) getEditorInput();
		String currentVVID = ei.getId();
		setPartProperty(VV_ID_EDITOR_PROPERTY_KEY, currentVVID);

		Composite comp = new Composite(parent, SWT.EMBEDDED);
		Frame awt = SWT_AWT.new_Frame(comp);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		final VisualizationViewer<VisualizationNode, VisualizationEdge> vv = ei.getViewer();
		vv.setPreferredSize(panel.getSize());

		JungSelectionProvider jsp = new JungSelectionProvider();
		getSite().setSelectionProvider(jsp);

		GraphListener gl = new GraphListener(jsp,ei);

		DefaultModalGraphMouse<VisualizationNode, VisualizationEdge> graphMouse = new DefaultModalGraphMouse<VisualizationNode, VisualizationEdge>();
		graphMouse.setZoomAtMouse(true);

		ei.setMode(Mode.PICKING);
		graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
		
		graphMouse.add(gl);

		vv.setGraphMouse(graphMouse);

		panel.add(vv, BorderLayout.CENTER);

		panel.setVisible(true);
		awt.add(panel);
		

	}

	@Override
	public void setFocus() {
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part == this) {
			part.getSite().getWorkbenchWindow().getPartService().removePartListener(this);
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		// TODO Auto-generated method stub

	}

}
