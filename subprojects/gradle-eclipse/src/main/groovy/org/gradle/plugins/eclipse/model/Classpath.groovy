/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.plugins.eclipse.model

import org.gradle.api.internal.XmlTransformer
import org.gradle.api.internal.tasks.generator.XmlPersistableConfigurationObject

/**
 * Represents the customizable elements of an eclipse classpath file. (via XML hooks everything is customizable).
 *
 * @author Hans Dockter
 */
class Classpath extends XmlPersistableConfigurationObject {
    List<ClasspathEntry> entries = []

    Classpath(XmlTransformer xmlTransformer) {
        super(xmlTransformer)
    }

    @Override protected String getDefaultResourceName() {
        return 'defaultClasspath.xml'
    }

    @Override protected void load(Node xml) {
        xml.classpathentry.each { Node entryNode ->
            ClasspathEntry entry = null
            switch (entryNode.@kind) {
                case 'src':
                    def path = entryNode.@path
                    entry = path.startsWith('/') ? new ProjectDependency(entryNode) : new SourceFolder(entryNode)
                    break
                case 'var': entry = new Variable(entryNode)
                    break
                case 'con': entry = new Container(entryNode)
                    break
                case 'lib': entry = new Library(entryNode)
                    break
                case 'output': entry = new Output(entryNode)
                    break
            }
            if (entry) {
                entries.add(entry)
            }
        }
    }

    def configure(List entries) {
        this.entries.addAll(entries)
        this.entries.unique()
    }

    @Override protected void store(Node xml) {
        xml.classpathentry.each { xml.remove(it) }
        entries.each { ClasspathEntry entry ->
            entry.appendNode(xml)
        }
    }

    boolean equals(o) {
        if (this.is(o)) { return true }

        if (getClass() != o.class) { return false }

        Classpath classpath = (Classpath) o;

        if (entries != classpath.entries) { return false }

        return true
    }

    int hashCode() {
        int result;

        result = entries.hashCode();
        return result;
    }

    public String toString() {
        return "Classpath{" +
                "entries=" + entries +
                '}';
    }
}
