package dev.mdklatt.idea.util.password

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toMutableProperty
import javax.swing.JComponent
import javax.swing.JPasswordField


/**
 * Modal dialog for a password prompt.
 */
class PasswordDialog(title: String = "Password", private val prompt: String = "Password:") :
    DialogWrapper(false) {

    private var password = charArrayOf()

    init {
        init()
        setTitle(title)
    }

    /**
     * Prompt the user for the password.
     *
     * @return user input
     */
    fun getPassword(): CharArray? = if (showAndGet()) password else null

    /**
     * Define dialog contents.
     *
     * @return: dialog contents
     */
    override fun createCenterPanel(): JComponent {
        // https://plugins.jetbrains.com/docs/intellij/kotlin-ui-dsl-version-2.html
        return panel{
            row("${prompt}:") {
                cell(JPasswordField("", 20)).bind(
                    componentGet = JPasswordField::getPassword,
                    componentSet = { field, value -> field.text = value.joinToString("") },
                    prop = ::password.toMutableProperty()
                )
            }
        }
    }
}
