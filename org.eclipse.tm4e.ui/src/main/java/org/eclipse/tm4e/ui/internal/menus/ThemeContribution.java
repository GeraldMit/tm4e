/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.menus;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.text.TMPresentationReconciler;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.tm4e.ui.themes.ThemeAssociation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Contribute "Switch to theme" menu item with list of available themes.
 *
 */
public class ThemeContribution extends CompoundContributionItem implements IWorkbenchContribution {

	private IHandlerService handlerService;

	@Override
	public void initialize(IServiceLocator serviceLocator) {
		handlerService = serviceLocator.getService(IHandlerService.class);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		List<IContributionItem> items = new ArrayList<IContributionItem>();
		if (handlerService != null) {
			IEditorPart editorPart = getActivePart(handlerService.getCurrentState());
			if (editorPart != null) {
				IThemeManager manager = TMUIPlugin.getThemeManager();
				boolean dark = manager.isDarkEclipseTheme();
				ITheme[] themes = manager.getThemes(dark);
				if (themes != null) {
					String scopeName = TMPresentationReconciler.getTMPresentationReconciler(editorPart).getGrammar()
							.getScopeName();
					ITheme selectedTheme = manager.getThemeForScope(scopeName, dark);
					for (ITheme theme : themes) {
						IAction action = createAction(scopeName, theme);
						if (theme.equals(selectedTheme)) {
							action.setChecked(true);
						}
						IContributionItem item = new ActionContributionItem(action);
						items.add(item);
					}
				}

			}
		}
		return items.toArray(new IContributionItem[items.size()]);
	}

	private Action createAction(final String scopeName, final ITheme theme) {
		return new Action(theme.getName()) {
			@Override
			public void run() {
				IThemeManager manager = TMUIPlugin.getThemeManager();
				IThemeAssociation association = new ThemeAssociation(theme.getId(), scopeName);
				manager.registerThemeAssociation(association);
				try {
					manager.save();
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}
		};
	}

	private static IEditorPart getActivePart(IEvaluationContext context) {
		if (context == null)
			return null;

		Object activePart = context.getVariable(ISources.ACTIVE_PART_NAME);
		if ((activePart instanceof IEditorPart))
			return (IEditorPart) activePart;

		return null;
	}

}
