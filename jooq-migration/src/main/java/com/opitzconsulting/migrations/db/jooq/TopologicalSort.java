package com.opitzconsulting.migrations.db.jooq;

import java.util.*;
import org.jooq.ForeignKey;
import org.jooq.Table;

public class TopologicalSort {
    public static List<Table<?>> topologicalSort(List<Table<?>> tables) {
        // Get all tables from the database

        // Map each table to its outgoing edges (foreign keys)
        Map<Table<?>, List<ForeignKey<?, ?>>> graph = new HashMap<>();
        for (Table<?> table : tables) {
            List<ForeignKey<?, ?>> edges = new ArrayList<>();
            for (ForeignKey<?, ?> fk : table.getReferences()) {
                edges.add(fk);
            }
            graph.put(table, edges);
        }

        // Perform topological sort
        List<Table<?>> sortedTables = new ArrayList<>();
        Set<Table<?>> visited = new HashSet<>();
        Set<Table<?>> cycleStack = new HashSet<>();

        for (Table<?> table : tables) {
            if (!visited.contains(table)) {
                visit(table, visited, sortedTables, graph, cycleStack);
            }
        }

        Collections.reverse(sortedTables);
        return sortedTables;
    }

    // Recursive helper function for topological sort
    private static void visit(
            Table<?> table,
            Set<Table<?>> visited,
            List<Table<?>> sortedTables,
            Map<Table<?>, List<ForeignKey<?, ?>>> graph,
            Set<Table<?>> cycleStack) {
        if (cycleStack.contains(table)) {
            // We've encountered a cycle, but we'll continue processing
            System.out.println("cycle detected: " + cycleStack);
            System.out.println("current table:  " + table);
            return;
        }

        if (!visited.contains(table)) {
            visited.add(table);
            cycleStack.add(table);
            for (ForeignKey<?, ?> edge : graph.get(table)) {
                Table<?> targetTable = edge.getKey().getTable();
                visit(targetTable, visited, sortedTables, graph, cycleStack);
            }
            cycleStack.remove(table);
            sortedTables.add(table);
        }
    }
}
