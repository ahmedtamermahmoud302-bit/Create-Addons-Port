package dev.createport.kinetics;

import java.util.*;

/** Platform-independent rotation and stress calculation. RPM is signed; stress uses its magnitude. */
public final class KineticNetwork {
    public record Node(int id, double generatedRpm, double capacityPerRpm, double stressPerRpm) {
        public Node {
            if (!Double.isFinite(generatedRpm) || !Double.isFinite(capacityPerRpm)
                    || !Double.isFinite(stressPerRpm) || capacityPerRpm < 0 || stressPerRpm < 0)
                throw new IllegalArgumentException("Invalid kinetic node");
        }
    }
    public record Edge(int from, int to, double ratio) {
        public Edge {
            if (!Double.isFinite(ratio) || ratio == 0) throw new IllegalArgumentException("Invalid gear ratio");
        }
    }
    public enum Status { RUNNING, UNPOWERED, OVERSTRESSED, CONFLICT, OVERSPEED }
    public record Result(Map<Integer, Double> theoreticalRpm, double capacity, double stress, Status status) {
        public double speed(int id) { return status == Status.RUNNING ? theoreticalRpm.getOrDefault(id, 0d) : 0; }
    }
    public static Result solve(List<Node> nodes, List<Edge> edges, double maxRpm) {
        if (!Double.isFinite(maxRpm) || maxRpm <= 0) throw new IllegalArgumentException("Invalid speed limit");
        Map<Integer, Node> byId = new LinkedHashMap<>();
        Map<Integer, List<Edge>> adjacency = new HashMap<>();
        for (Node n : nodes) {
            if (byId.put(n.id(), n) != null) throw new IllegalArgumentException("Duplicate node");
            adjacency.put(n.id(), new ArrayList<>());
        }
        for (Edge e : edges) {
            if (!byId.containsKey(e.from()) || !byId.containsKey(e.to())) throw new IllegalArgumentException("Unknown node");
            adjacency.get(e.from()).add(e);
            adjacency.get(e.to()).add(new Edge(e.to(), e.from(), 1 / e.ratio()));
        }
        // One solve represents one connected component, including passive components.
        if (!nodes.isEmpty()) {
            Set<Integer> visited = new HashSet<>();
            ArrayDeque<Integer> pending = new ArrayDeque<>(); pending.add(nodes.get(0).id());
            while (!pending.isEmpty()) {
                int id = pending.remove(); if (!visited.add(id)) continue;
                for (Edge e : adjacency.get(id)) pending.add(e.to());
            }
            if (visited.size() != nodes.size()) throw new IllegalArgumentException("Disconnected network");
        }
        Node source = nodes.stream().filter(n -> n.generatedRpm() != 0).findFirst().orElse(null);
        if (source == null) return new Result(Map.of(), 0, 0, Status.UNPOWERED);
        Map<Integer, Double> speeds = new LinkedHashMap<>();
        ArrayDeque<Integer> pending = new ArrayDeque<>();
        speeds.put(source.id(), source.generatedRpm()); pending.add(source.id());
        boolean conflict = false;
        while (!pending.isEmpty()) {
            int id = pending.remove();
            for (Edge e : adjacency.get(id)) {
                double expected = speeds.get(id) * e.ratio();
                Double existing = speeds.putIfAbsent(e.to(), expected);
                if (existing == null) pending.add(e.to());
                else if (Math.abs(existing - expected) > 0.0001) conflict = true;
            }
        }
        double capacity = 0, stress = 0; boolean overspeed = false;
        for (Node n : nodes) {
            double rpm = speeds.get(n.id());
            if (n.generatedRpm() != 0 && Math.abs(n.generatedRpm() - rpm) > 0.0001) conflict = true;
            capacity += n.capacityPerRpm() * Math.abs(n.generatedRpm());
            stress += n.stressPerRpm() * Math.abs(rpm);
            overspeed |= !Double.isFinite(rpm) || Math.abs(rpm) > maxRpm;
        }
        Status status = conflict ? Status.CONFLICT : overspeed ? Status.OVERSPEED
                : stress > capacity ? Status.OVERSTRESSED : Status.RUNNING;
        return new Result(Map.copyOf(speeds), capacity, stress, status);
    }
    private KineticNetwork() {}
}
