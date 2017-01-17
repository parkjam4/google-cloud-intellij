/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.intellij.vcs;

import com.google.cloud.tools.intellij.login.CredentialedUser;
import com.google.cloud.tools.intellij.resources.ProjectSelector;
import com.google.cloud.tools.intellij.resources.RepositoryRemotePanel;
import com.google.cloud.tools.intellij.resources.RepositorySelector;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;

import git4idea.repo.GitRepository;

/**
 * Shows a dialog that has one entry value which is a GCP project using the project selector. The
 * title and ok button text is passed into the constructor.
 */
public class UploadSourceDialog extends DialogWrapper {

  private JPanel rootPanel;
  private ProjectSelector projectSelector;
  private RepositorySelector repositorySelector;
  private RepositoryRemotePanel remoteNameSelector;
  private String projectId;
  private String repositoryId;
  private String remoteName;
  private CredentialedUser credentialedUser;
  private GitRepository gitRepository;

  /**
   * Initialize the project selection dialog.
   */
  public UploadSourceDialog(@NotNull Project project, @Nullable GitRepository gitReository,
      @NotNull String title, @NotNull String okText) {
    super(project, true);

    this.gitRepository = gitReository;

    init();
    setTitle(title);
    setOKButtonText(okText);
    setOKActionEnabled(false);
  }

  /**
   * Return the project ID selected by the user.
   */
  @NotNull
  public String getProjectId() {
    return projectId;
  }

  @NotNull
  String getRepositoryId() {
    return repositoryId;
  }

  @NotNull
  public String getRemoteName() {
    return remoteName;
  }

  /**
   * Return the credentialeduser that owns the ID returned from {@link #getProjectId()}.
   */
  @Nullable
  public CredentialedUser getCredentialedUser() {
    return credentialedUser;
  }

  @Override
  protected String getDimensionServiceKey() {
    return "UploadSourceDialog";
  }

  private void createUIComponents() {
    projectSelector = new ProjectSelector();
    projectSelector.setMinimumSize(new Dimension(300, 0));

    repositorySelector = new RepositorySelector(projectSelector.getText(),
        projectSelector.getSelectedUser(), true /*canCreateRepository*/);

    remoteNameSelector = new RepositoryRemotePanel(gitRepository);

    projectSelector.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent event) {
        repositorySelector.setCloudProject(projectSelector.getText());
        repositorySelector.setUser(projectSelector.getSelectedUser());
        repositorySelector.setText("");
        updateButtons();
      }
    });

    repositorySelector.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        remoteNameSelector.update(repositorySelector.getSelectedRepository());
        updateButtons();
      }
    });

    remoteNameSelector.getRemoteNameField().getDocument().addDocumentListener(
        new DocumentAdapter() {
          @Override
          protected void textChanged(DocumentEvent e) {
            updateButtons();
          }
        });
  }

  private void updateButtons() {
    if (!StringUtil.isEmpty(projectSelector.getText())
        && projectSelector.getSelectedUser() == null) {
      setErrorText("Invalid Cloud Project selected.");
      setOKActionEnabled(false);
      return;
    } else if (!StringUtil.isEmpty(repositorySelector.getText())
        && StringUtil.isEmpty(repositorySelector.getSelectedRepository())) {
      setErrorText("Invalid Cloud Repository selected.");
      setOKActionEnabled(false);
      return;
    } else if(StringUtil.isEmpty(remoteNameSelector.getText())) {
      setErrorText("Enter a remote name.");
      setOKActionEnabled(false);
      return;
    } else if(projectSelector.getSelectedUser() == null
        || StringUtil.isEmpty(repositorySelector.getSelectedRepository())) {
      setErrorText(null);
      setOKActionEnabled(false);
      return;
    }

    setErrorText(null);
    setOKActionEnabled(true);
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return projectSelector;
  }

  @Override
  protected JComponent createCenterPanel() {
    return rootPanel;
  }

  @Override
  protected void doOKAction() {
    projectId = projectSelector.getText();
    repositoryId = repositorySelector.getText();
    remoteName = remoteNameSelector.getText();
    credentialedUser = projectSelector.getSelectedUser();
    super.doOKAction();
  }
}
