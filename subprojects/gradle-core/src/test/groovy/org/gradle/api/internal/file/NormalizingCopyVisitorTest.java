/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.internal.file;

import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.RelativePath;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.integration.junit4.JMock;

import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class NormalizingCopyVisitorTest {
    private final JUnit4Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private final CopySpecVisitor delegate = context.mock(CopySpecVisitor.class);
    private final NormalizingCopyVisitor visitor = new NormalizingCopyVisitor(delegate);

    @Test
    public void visitsADirectoryWhenItHasNotBeenVisitedBefore() {
        final FileVisitDetails details = file("dir");

        context.checking(new Expectations() {{
            one(delegate).visitDir(details);
        }});

        visitor.visitDir(details);
    }

    @Test
    public void doesNotVisitADirectoryWhichHasBeenVisitedBefore() {
        final FileVisitDetails details = file("dir");

        context.checking(new Expectations() {{
            one(delegate).visitDir(details);
        }});

        visitor.visitDir(details);
        visitor.visitDir(details);
    }

    @Test
    public void visitsDirectoryAncestorsWhichHaveNotBeenVisited() {
        final FileVisitDetails dir1 = file("a/b/c");

        context.checking(new Expectations() {{
            one(delegate).visitDir(with(hasPath("a")));
            one(delegate).visitDir(with(hasPath("a/b")));
            one(delegate).visitDir(dir1);
        }});

        visitor.visitDir(dir1);

        final FileVisitDetails dir2 = file("a/b/d/e");

        context.checking(new Expectations() {{
            one(delegate).visitDir(with(hasPath("a/b/d")));
            one(delegate).visitDir(dir2);
        }});

        visitor.visitDir(dir2);
    }

    @Test
    public void visitsFileAncestorsWhichHaveNotBeenVisited() {
        final FileVisitDetails details = file("a/b/c");

        context.checking(new Expectations() {{
            one(delegate).visitDir(with(hasPath("a")));
            one(delegate).visitDir(with(hasPath("a/b")));
            one(delegate).visitFile(details);
        }});

        visitor.visitFile(details);
    }

    @Test
    public void visitsSpecRootDir() {
        final CopySpecImpl spec = context.mock(CopySpecImpl.class);

        context.checking(new Expectations() {{
            allowing(spec).getDestPath();
            will(returnValue(RelativePath.parse(false, "a/b")));
            one(delegate).visitDir(with(hasPath("a")));
            one(delegate).visitDir(with(hasPath("a/b")));
            one(delegate).visitSpec(spec);
        }});

        visitor.visitSpec(spec);
    }

    private FileVisitDetails file(final String path) {
        final FileVisitDetails details = context.mock(FileVisitDetails.class, path);
        context.checking(new Expectations(){{
            allowing(details).getRelativePath();
            will(returnValue(RelativePath.parse(false, path)));
        }});
        return details;
    }

    private Matcher<FileVisitDetails> hasPath(final String path) {
        return new BaseMatcher<FileVisitDetails>() {
            public void describeTo(Description description) {
                description.appendText("has path ").appendValue(path);
            }

            public boolean matches(Object o) {
                FileVisitDetails details = (FileVisitDetails) o;
                return details.getRelativePath().getPathString().equals(path);
            }
        };
    }
}