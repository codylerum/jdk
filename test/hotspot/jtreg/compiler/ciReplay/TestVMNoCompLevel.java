/*
 * Copyright (c) 2016, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * @test
 * @bug 8011675
 * @library / /test/lib
 * @summary testing of ciReplay with using generated by VM replay.txt w/o comp_level
 * @requires vm.flightRecorder != true & vm.compMode != "Xint" & vm.debug == true &
 *           (vm.opt.TieredStopAtLevel == null | vm.opt.TieredStopAtLevel == 1 | vm.opt.TieredStopAtLevel == 4)
 * @modules java.base/jdk.internal.misc
 * @build sun.hotspot.WhiteBox
 * @run driver jdk.test.lib.helpers.ClassFileInstaller sun.hotspot.WhiteBox
 * @run main/othervm -Xbootclasspath/a:. -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI
 *      compiler.ciReplay.TestVMNoCompLevel
 */

package compiler.ciReplay;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class TestVMNoCompLevel extends CiReplayBase {
    public static void main(String args[]) {
        new TestVMNoCompLevel().runTest(false);
    }

    @Override
    public void testAction() {
        try {
            Path replayFilePath = Paths.get(REPLAY_FILE_NAME);
            List<String> replayContent = Files.readAllLines(replayFilePath);
            for (int i = 0; i < replayContent.size(); i++) {
                String line = replayContent.get(i);
                if (line.startsWith("compile ")) {
                    replayContent.set(i, line.substring(0, line.lastIndexOf(" ")));
                }
            }
            Files.write(replayFilePath, replayContent, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ioe) {
            throw new Error("Failed to read/write replay data: " + ioe, ioe);
        }
        if (CLIENT_VM_AVAILABLE) {
            if (SERVER_VM_AVAILABLE) {
                negativeTest(CLIENT_VM_OPTION);
            } else {
                positiveTest(CLIENT_VM_OPTION);
            }
        }
        if (SERVER_VM_AVAILABLE) {
            positiveTest(TIERED_DISABLED_VM_OPTION, SERVER_VM_OPTION);
            positiveTest(TIERED_ENABLED_VM_OPTION, SERVER_VM_OPTION);
        }
    }
}

