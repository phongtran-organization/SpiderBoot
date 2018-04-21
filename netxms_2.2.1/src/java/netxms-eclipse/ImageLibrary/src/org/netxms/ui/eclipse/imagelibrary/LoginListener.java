package org.netxms.ui.eclipse.imagelibrary;

import java.util.UUID;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.netxms.client.NXCSession;
import org.netxms.client.SessionListener;
import org.netxms.client.SessionNotification;
import org.netxms.ui.eclipse.console.api.ConsoleLoginListener;
import org.netxms.ui.eclipse.imagelibrary.shared.ImageProvider;

/**
 * Early startup handler
 */
public class LoginListener implements ConsoleLoginListener {
	private final class ImageLibraryListener implements SessionListener {
		private ImageLibraryListener(Display display, NXCSession session) {
		}

		@Override
		public void notificationHandler(SessionNotification n) {
			if (n.getCode() == SessionNotification.IMAGE_LIBRARY_CHANGED) {
				final UUID guid = (UUID) n.getObject();
				final ImageProvider imageProvider = ImageProvider.getInstance();
				imageProvider.invalidateImage(guid,
						n.getSubCode() == SessionNotification.IMAGE_DELETED);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.console.api.ConsoleLoginListener#afterLogin(org
	 * .netxms.client.NXCSession, org.eclipse.swt.widgets.Display)
	 */
	@Override
	public void afterLogin(final NXCSession session, final Display display) {
		ImageProvider.createInstance(display, session);
		Job job = new Job(Messages.get().LoginListener_JobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ImageProvider.getInstance().syncMetaData();
					session.addListener(new ImageLibraryListener(display,
							session));
				} catch (Exception e) {
					// FIXME
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}
}
