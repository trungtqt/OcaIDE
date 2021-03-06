package ocaml.popup.actions;

import java.util.Collection;

import ocaml.OcamlPlugin;
import ocaml.util.Misc;
import ocaml.views.OcamlCompilerOutput;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This action is called when the user clicks on the "make project" menu item in the contextual menu for
 * OCaml makefile projects. Calls the Builder to build the project.
 */
public class CancelCompileProjectPopupAction implements IObjectActionDelegate {

	public CancelCompileProjectPopupAction() {
	}

	/** This method is used to call the action directly, without having to instantiate it */
	public static void cancelCompileProject(IProject project) {
		CancelCompileProjectPopupAction action = new CancelCompileProjectPopupAction();
		action.run(null);
	}

	@Override
	public void run(IAction action) {
		final String jobName = "Cancelling compiling jobs";

		// show compiler output
		Misc.showView(OcamlCompilerOutput.ID);

		Job job = new Job(jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (!OcamlPlugin.ActiveBuildJobs.isEmpty()) {
					// cancel all jobs
					Collection<IProgressMonitor> monitors = OcamlPlugin.ActiveBuildJobs.values();
					for (IProgressMonitor m: monitors) {
						m.setCanceled(true);
					}
					// clear them from store
					OcamlPlugin.ActiveBuildJobs.clear();
				}
				return Status.OK_STATUS;
			}
		};

		job.setPriority(Job.BUILD);
		job.setUser(action != null);
		job.schedule(50);
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
