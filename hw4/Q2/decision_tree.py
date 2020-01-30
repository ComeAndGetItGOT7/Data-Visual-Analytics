from util import entropy, information_gain, partition_classes
import numpy as np 
import ast

class DecisionTree(object):
    def __init__(self):
        # Initializing the tree as an empty dictionary or list, as preferred
        #self.tree = []
        self.tree = {}

    def learn(self, X, y):
        # TODO: Train the decision tree (self.tree) using the the sample X and labels y
        # You will have to make use of the functions in utils.py to train the tree
        
        # One possible way of implementing the tree:
        #    Each node in self.tree could be in the form of a dictionary:
        #       https://docs.python.org/2/library/stdtypes.html#mapping-types-dict
        #    For example, a non-leaf node with two children can have a 'left' key and  a 
        #    'right' key. You can add more keys which might help in classification
        #    (eg. split attribute and split value)

        """y consists one kind of label"""
        if y.count(y[0]) == len(y):
            self.tree['label'] = y[0]
            self.tree['left'] = None
            self.tree['right'] = None
            return self.tree

        """All samples have the same attribute values"""
        if X.count(X[0]) == len(X):
            attr_count = {}
            for label in y:
                if label in attr_count.keys():
                    attr_count[label] += 1
                else:
                    attr_count[label] = 1
            most_freq = max(attr_count, key = attr_count.get)
            self.tree['label'] = most_freq
            self.tree['left'] = None
            self.tree['right'] = None
            return self.tree

        max_gain = 0
        max_attr = 0
        value = 0
        m = int(np.sqrt(len(X[0])))
        attr_num = list(range(len(X[0])))
        """randomly choose m attributes"""
        attr_selected = np.random.choice(attr_num, m, replace=False)
        attr_selected = sorted(attr_selected)
        for j in range(len(attr_selected)):
            i = attr_selected[j]
            data_selected = [data[i] for data in X]
            avg = float(np.mean(data_selected))
            partition = partition_classes(X, y, i, avg)
            curr_y = [partition[2], partition[3]]
            curr_gain = information_gain(y, curr_y)
            if curr_gain >= max_gain:
                max_gain = curr_gain
                max_attr = i
                value = avg

        self.tree["attr"] = max_attr
        self.tree["value"] = value
        X_left, X_right, y_left, y_right = partition_classes(X, y, max_attr, value)
        left = DecisionTree()
        right = DecisionTree()
        left.tree = left.learn(X_left, y_left)
        right.tree = right.learn(X_right, y_right)
        self.tree["left"] = left.tree
        self.tree["right"] = right.tree
        return self.tree

    def classify(self, record):
        # TODO: classify the record using self.tree and return the predicted label
        """Travel through the tree until a leaf is met"""
        curr = self.tree
        while curr["left"] or curr["right"]:
            curr_attr = curr["attr"]
            curr_value = curr["value"]
            if type(curr_value) is int or type(curr_value) is float:
                if record[curr_attr] <= curr_value:
                    curr = curr["left"]
                else:
                    curr = curr["right"]
            else:
                if record[curr_attr] is curr_value:
                    curr = curr["left"]
                else:
                    curr = curr["right"]

        return curr['label']

