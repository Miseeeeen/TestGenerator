package borMi.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


// 目的是切割式子, 使它的一部分用BOR解决, 另一部分用MI解决
// 假设切割对象是dnf, 所以处理完之后, 所有的partition都是由"析取"连接的
public class Classifier {
    ArrayList<Partition> partitions = new ArrayList<Partition>();
    Iterator iterator;

    public Classifier(String predicate) {
        predicate = removeBrackets(predicate); // 因为是析取范式, 所以去掉括号不会有影响
        partitions = divideByOr(predicate); // 根据析取符来切割式子
        merge(partitions); // 合并拥有common variables的partition, 使得所有的partition都互为奇异式

        initIterator();
    }

    private void merge(ArrayList<Partition> partitions) {
        while (hasUnkownPart(partitions)) {
            int i = 0;
            while (!partitions.get(i).isUnkown()) {
                i++;
            }
            Partition part = partitions.get(i);

            ArrayList<Partition> pastPartitions = null;

            while (canBeAugmented(partitions, pastPartitions)) {
                pastPartitions = partitions;
                for (int j = i + 1; j < partitions.size(); j++) {
                    if (partitions.get(j).isUnkown()
                            && hasCommonVar(part.predicate, partitions.get(j).predicate)) {
                        part.predicate = part.predicate + "|" + partitions.get(j).predicate;
                        partitions.remove(j--); // 这里的remove会引起partitions的变动, caution!!!
                    }
                }
            }

            if (!hasCommonVarInside(part.predicate)) {
                part.setSingular();
            }
            else {
                part.setNonseparable();
            }
        }
    }

    private boolean canBeAugmented(ArrayList<Partition> partitions,
            ArrayList<Partition> pastPartitions) {
        return !partitions.equals(pastPartitions);
    }

    private boolean hasCommonVarInside(String pr) {
        if (pr.contains("|")) {
            return true;
        }
        String[] tmp = pr.split("&|!");
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (String s : tmp) {
            if (map.containsKey(s)) {
                return true;
            }
            map.put(s, 1);
        }
        return false;
    }

    private boolean hasUnkownPart(ArrayList<Partition> partitions) {
        for (Partition p : partitions) {
            if (p.isUnkown()) {
                return true;
            }
        }
        return false;
    }

    private void initIterator() {
        iterator = partitions.iterator();
    }

    private ArrayList<Partition> divideByOr(String pr) {
        ArrayList<Partition> partitions = new ArrayList<Partition>();
        String[] tmp = pr.split("\\|");
        for (String s : tmp) {
            partitions.add(new Partition(s));
        }

        return partitions;
    }

    private boolean hasCommonVar(String pr1, String pr2) { // 判断两个式子有没有common variables
        pr1 = pr1.replaceAll("!", ""); // '!'本来应该放在正则表达式里分割, 但不知道为啥有问题, 临时这样解决
        pr2 = pr2.replaceAll("!", "");
        String[] tmp1 = pr1.split("&|\\|"); // 与或为分隔符
        String[] tmp2 = pr2.split("&|\\|");

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (String s : tmp2) {
            map.put(s, 1);
        }
        for (String s : tmp1) {
            if (map.containsKey(s)) {
                return true;
            }
        }
        return false;
    }

    private String removeBrackets(String pr) {
        return pr.replaceAll("\\(|\\)", "");
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Partition nextPartition() {
        return (Partition) iterator.next();
    }
}
