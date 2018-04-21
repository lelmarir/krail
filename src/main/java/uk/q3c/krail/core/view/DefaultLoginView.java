/*
 * Copyright (c) 2014 David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package uk.q3c.krail.core.view;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import uk.q3c.krail.core.shiro.LoginExceptionHandler;
import uk.q3c.krail.core.shiro.SubjectProvider;
import uk.q3c.krail.i18n.*;
import uk.q3c.util.ID;

import java.util.Optional;

public class DefaultLoginView extends GridViewBase implements  ClickListener {
    private final Provider<Subject> subjectProvider;
    private Panel centrePanel;
    private Label demoInfoLabel;
    private Label demoInfoLabel2;
    private Label label;
    private PasswordField passwordBox;
    private Label statusMsgLabel;
    private Button submitButton;
    private TextField usernameBox;

    @Inject
	protected DefaultLoginView(SubjectProvider subjectProvider) {
        super();
        this.subjectProvider = subjectProvider;

		getRootComponent().setColumns(3);
		getRootComponent().setRows(3);
		getRootComponent().setSizeFull();
        centrePanel = new Panel();
        centrePanel.addStyleName(ValoTheme.PANEL_WELL);
        centrePanel.setSizeUndefined();
        VerticalLayout vl = new VerticalLayout();
        centrePanel.setContent(vl);
        vl.setSpacing(true);
        vl.setSizeUndefined();
        label = new Label();
        usernameBox = new TextField();
        passwordBox = new PasswordField();

        demoInfoLabel = new Label("for this demo, enter any user name, and a password of 'password'");
        demoInfoLabel2 = new Label("In a real application your Shiro Realm implementation defines how to authenticate");

        submitButton = new Button();
        submitButton.setDisableOnClick(true);
        submitButton.addClickListener(this);

        statusMsgLabel = new Label("Please enter your username and password");

        vl.addComponent(label);
        vl.addComponent(demoInfoLabel);
        vl.addComponent(demoInfoLabel2);
        vl.addComponent(usernameBox);
        vl.addComponent(passwordBox);
        vl.addComponent(submitButton);
        vl.addComponent(statusMsgLabel);

		getRootComponent().addComponent(centrePanel, 1, 1);
		getRootComponent().setColumnExpandRatio(0, 1);
		getRootComponent().setColumnExpandRatio(2, 1);

		getRootComponent().setRowExpandRatio(0, 1);
		getRootComponent().setRowExpandRatio(2, 1);
    }

    @Override
    protected void setIds() {
        super.setIds();
        submitButton.setId(ID.getId(Optional.empty(), this, submitButton));
        usernameBox.setId(ID.getId(Optional.of("username"), this, usernameBox));
        passwordBox.setId(ID.getId(Optional.of("password"), this, passwordBox));
        statusMsgLabel.setId(ID.getId(Optional.of("status"), this, statusMsgLabel));
    }


    @Override
    public void buttonClick(ClickEvent event) {
		submitButton.setEnabled(false);

		UsernamePasswordToken token = new UsernamePasswordToken(
				usernameBox.getValue(), passwordBox.getValue());

		//FIXME: localizazzione
        try {
			Subject subject = subjectProvider.get();
			subject.login(token);
        } catch (UnknownAccountException uae) {
			setStatusMessage("Unknown Account");
        } catch (IncorrectCredentialsException ice) {
			setStatusMessage("Invalid_Login");
        } catch (ExpiredCredentialsException ece) {
			setStatusMessage("Account_Expired");
        } catch (LockedAccountException lae) {
			setStatusMessage("Account_Locked");
        } catch (ExcessiveAttemptsException excess) {
			setStatusMessage("Too_Many_Login_Attempts");
        } catch (DisabledAccountException dae) {
			setStatusMessage("Account_is_Disabled");
        } catch (ConcurrentAccessException cae) {
			setStatusMessage("Account_Already_In_Use");
        } catch (AuthenticationException ae) {
			setStatusMessage("Generic_Authentication_Exception");
		} finally {
			submitButton.setEnabled(true);
        }
        // unexpected condition - error?
        // an exception would be raised if login failed
    }

    public void setStatusMessage(String msg) {
        statusMsgLabel.setValue(msg);
    }
}
