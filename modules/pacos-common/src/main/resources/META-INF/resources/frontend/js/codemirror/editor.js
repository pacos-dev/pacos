import {Compartment, EditorState} from "@codemirror/state"
import {xmlLanguage} from "@codemirror/lang-xml"
import {jsonLanguage} from "@codemirror/lang-json"
import {htmlLanguage} from "@codemirror/lang-html"
import {javascriptLanguage} from "@codemirror/lang-javascript"
import {javaLanguage} from "@codemirror/lang-java"
import {StandardSQL} from "@codemirror/lang-sql"
import {html, LitElement} from "lit";
import {EditorView} from "@codemirror/view";
import {basicSetup} from "codemirror";
import {autocompletion} from '@codemirror/autocomplete';

export class MyNativeCodeEditor extends LitElement {

    static get properties() {
        return {
            value: String,
            ident: String,
            lang: String,
            editor: {type: EditorView},
            language: {type: Compartment},
            completition: {type: Array},
            scope: {type: Array},
        }
    }

    constructor() {
        super();
        this.value = "";
        this.internalUpdate = false; // prevent loop on update
    }

    createRenderRoot() {
        this.ident = "code" + Date.now();
        return this;
    }

    attributeChangedCallback(name, oldValue, newValue) {
        if (name === 'completition') {
            this.completition = newValue;
        }
    }

    render() {
        return html`
            <style>
                .ͼ1.cm-editor {
                    height: 100%;
                }
            </style>
            <div id="${this.ident}" style="width:100%;height:100%"></div>`;
    }

    firstUpdated() {
        this.language = new Compartment();
        const element = document.getElementById(this.ident);
        const parent = document.createElement("div");
        document.body.append(parent);

        const vModal = document.querySelector('v-modal');

        if (vModal) {
            vModal.initializeScope(this.scope);
        } else {
            console.error('Variable modal dont exists. Cant initialize variables');
        }

        const extensions = [basicSetup,
            this.language.of(this.getLang()),
            autocompletion(),
            this.getLang().data.of({
                autocomplete: (editor) => this.myCompletions(editor, this.scope, this.completition)
            }),
            EditorView.updateListener.of((v) => {
                if (v.docChanged) {
                    this.internalUpdate = true;
                    this.value = v.state.doc.toString();
                    this.dispatchEvent(new CustomEvent("value-changed", {
                        detail: {value: this.value},
                        bubbles: true, composed: true
                    }));
                }
            })]

        this.editor = new EditorView({
            state: EditorState.create({
                doc: this.value,
                extensions: extensions,
            }),
            parent: parent
        });

        document.body.removeChild(parent);
        element.appendChild(parent.childNodes[0]);
    }


    /** Calculate completitions based on current scope and completitions received from the backend**/
    myCompletions(context, scopes, completions) {
        let word = context.matchBefore(/\w*/)
        if (word.from === word.to && !context.explicit) {
            return null
        }

        let newArray = [];
        if (scopes) {
            for (const scope of scopes) {
                const variables = document.querySelector('v-modal')
                    .storage().getForScope(scope.id + "#" + scope.name);
                for (const variable of variables) {
                    const newItem = {
                        label: variable.name,
                        apply: "{{" + variable.name + "}}",
                        type: "variable",
                        info: "Current value: " + variable.value() + " -- " + variable.description
                    };
                    newArray.push(newItem);
                }
            }
        }

        if (completions) {
            newArray = newArray.concat(completions);
        }
        return {
            from: word.from,
            options: newArray
        }
    }

    setEditorValue(v) {
        if (typeof v == "undefined") {
            v = "";
        }
        if (typeof this.editor == "undefined") {
            return;
        }
        this.editor.update([{changes: {from: 0, to: this.editor.state.doc.length, insert: v}}]);
    }

    getLang() {
        if (this.lang === 'XML') {
            return xmlLanguage;
        } else if (this.lang === 'JSON') {
            return jsonLanguage;
        } else if (this.lang === 'HTML') {
            return htmlLanguage;
        } else if (this.lang === 'JAVASCRIPT') {
            return javascriptLanguage;
        } else if (this.lang === 'JAVA') {
            return javaLanguage;
        } else if (this.lang === 'SQL') {
            return StandardSQL.language;
        }
        return jsonLanguage;
    }

    updated(changedProperties) {
        super.updated(changedProperties);
        if (changedProperties.has("value") && this.editor) {
            if (this.internalUpdate) {
                this.internalUpdate = false; // ignore update
                return;
            }
            // console.log('value  updated :(' + changedProperties.get('value'))
            this.editor.dispatch({
                changes: {from: 0, to: this.editor.state.doc.length, insert: this.value || ""}
            });
        }
    }

    //@Server -> @Client
    setLanguage(value) {
        this.lang = value;
        if (!this.editor) {
            return;
        }

        this.editor.dispatch({
            effects: this.language.reconfigure(this.getLang())
        })
    }


    //@Server -> @Client
    setValueFromServer(value) {
        this.setEditorValue(value);
    }

    //@Server -> @Client ->@Server
    getCodeFromClient() {
        return this.editor.state.doc.toString();
    }


    //@Server -> @Client ->@Server
    getValueWithRangeSelection() {
        let result = [];
        result.push(this.editor.state.doc.toString());

        let range = this.editor.state.selection.ranges.at(0);
        result.push(range.from);
        result.push(range.to);
        return result;
    }
}

customElements.define('native-code-editor', MyNativeCodeEditor);