package com.tangzhixiong.jwatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayDeque;
import java.util.HashSet;

public class Main {
    public static WatchService watchService = null;
    public static Runtime runtime = null;
    public static HashSet<String> ignoredDirs = new HashSet<>();
    static {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (Exception e) {
            e.printStackTrace();
        }
        runtime = Runtime.getRuntime();
    }

    // TODO: java regex to load .jwatchignore

    public static void register(String srcDirPath) {
        final ArrayDeque<File> queue = new ArrayDeque<>();
        File srcDirFile = new File(srcDirPath);
        if (!srcDirFile.exists() || !srcDirFile.isDirectory()) {
            System.err.format("[%s] is not a valid directory for watching.", srcDirPath);
            return;
        }
        try {
            System.out.println("Watching directories:");
            queue.add(srcDirFile);
            while (!queue.isEmpty()) {
                File pwd = queue.poll();
                System.out.format("--- %s\n", pwd.getCanonicalPath().replace("\\", "/"));
                try {
                    WatchKey key = pwd.toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                final File[] entries;
                try {
                    entries = pwd.listFiles();
                } catch (NullPointerException e) { continue; }

                for (File entry: entries) {
                    if (entry.isDirectory()) {
                        final String basename = entry.getName();
                        if (!basename.startsWith(".") && !ignoredDirs.contains(basename)) {
                            queue.add(entry);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String srcDir = ".";
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-d") || args[i].equals("--directory")) {
                if (++i < args.length) { srcDir = args[i]; }
            } else if (args[i].equals("-i") || args[i].equals("--ignore")) {
                if (++i < args.length) {
                    for (String dir : args[i].split(";")) {
                        ignoredDirs.add(dir);
                    }
                }
            }
        }

        register(srcDir);
        int counter = 0;
        System.out.println("[ ] watching...");
        while (true) {
            WatchKey key = null;
            try {
                key = watchService.take();
                if (key == null) {
                    throw new InterruptedException();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
            System.out.format("\n\n\n\n[ ] watching... (#%d)\n", ++counter);
            if (!key.pollEvents().isEmpty()) {
                try {
                    System.out.println("[*] making...");
                    Process p = new ProcessBuilder().inheritIO().command("make").start();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!key.reset()) {
                break;
            }
        }
    }
}
