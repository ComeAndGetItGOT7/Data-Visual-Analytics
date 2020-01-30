import http.client
import json
import time
import timeit
import sys
import collections
from pygexf.gexf import *


#
# implement your data retrieval code here
#

APIkey=sys.argv[1]
hds={
"Accept":"application/json",
"Authorization":"key "+APIkey
}

conn=http.client.HTTPConnection("www.rebrickable.com")


#Q1.1a
MIN_PARTS=1140
requestURL="https://rebrickable.com/api/v3/lego/sets/?page=1&page_size=350&min_parts="+str(
    MIN_PARTS)+"&ordering=-num_parts"
conn.request('GET',requestURL,headers=hds)
read=conn.getresponse().read().decode('utf8')
read=json.loads(read)
results=read["results"]
set_info={'set_num':[],'name':[]}
for result in results:
    set_info['set_num'].append(result["set_num"])
    set_info['name'].append(result["name"])

#Q1.1b
import time
def takeQuantity(d):
    return d["quantity"]

part_info={'set_num':[],'part_name':[],'part_id':[],'part_quantity':[]} 
t1=time.time()
i=0
for num in set_info["set_num"]:
    i=i+1
    print("%d\t"%i)
    requestURL="https://rebrickable.com/api/v3/lego/sets/"+num+"/parts/?page_size=1000"
    conn.request('GET',requestURL,headers=hds)
    print("Finish requesting")
    invt_part=conn.getresponse().read().decode('utf8')
    invt_part=json.loads(invt_part)
    invt_part_results=invt_part["results"]
    invt_part_results.sort(key=takeQuantity,reverse=True)
    if(len(invt_part_results)>20):
        invt_part_results_top20=invt_part_results[0:20]
    else:
        invt_part_results_top20=invt_part_results
    for rslt in invt_part_results_top20:
        part_info['set_num'].append(num)
        part_info['part_name'].append(rslt["part"]["name"])
        part_info['part_id'].append(rslt["part"]["part_num"]+rslt["color"]["rgb"])
        part_info['part_quantity'].append(rslt["quantity"])
t2=time.time()
print("Costs %0.2f sec in total."%(t2-t1))

conn.close()


#Q1.1c&d
gexf = Gexf("Yanjun Ding","Rebrickable Lego Data")
graph=gexf.addGraph("undirected","static","Rebrickable Lego Data")

type_set = graph.addNodeAttribute("set",type='string')
type_part= graph.addNodeAttribute("part",type='string')

for i in range(0,len(set_info["set_num"])):
    if(graph.nodeExists(set_info["set_num"][i])==0):
        node_set=graph.addNode(set_info["set_num"][i],set_info["name"][i])
        node_set.addAttribute(type_set,set_info["name"][i])
        node_set.setColor('0','0','0')

for i in range(0,len(part_info["part_name"])):
    if(graph.nodeExists(part_info["part_id"][i])==0):
        node_part=graph.addNode(part_info["part_id"][i],part_info["part_name"][i])
        node_part.addAttribute(type_part,part_info["part_name"][i])
        r,g,b=part_info["part_id"][i][-6:-4],part_info["part_id"][i][-4:-2],part_info["part_id"][i][-2:]
        node_part.setColor(str(int(r,16)),str(int(g,16)),str(int(b,16)))

for i in range(0,len(part_info["part_name"])):
    graph.addEdge(str(i),part_info["set_num"][i],part_info["part_id"][i],weight=part_info["part_quantity"][i])

output_file=open("bricks_graph.gexf","wb")
gexf.write(output_file)
output_file.close()



# complete auto grader functions for Q1.1.b,d
def min_parts():
    """
    Returns an integer value
    """
    # you must replace this with your own value
    return MIN_PARTS

def lego_sets():
    """
    return a list of lego sets.
    this may be a list of any type of values
    but each value should represent one set

    e.g.,
    biggest_lego_sets = lego_sets()
    print(len(biggest_lego_sets))
    > 280
    e.g., len(my_sets)
    """
    # you must replace this line and return your own list
    """
    this func returns set_num in a list.
    """
    global set_info
    return set_info['set_num']

def gexf_graph():
    """
    return the completed Gexf graph object
    """
    # you must replace these lines and supply your own graph
    global gexf
    return gexf.graphs[0]

# complete auto-grader functions for Q1.2.d

def avg_node_degree():
    """
    hardcode and return the average node degree
    (run the function called “Average Degree”) within Gephi
    """
    # you must replace this value with the avg node degree
    return 5.424

def graph_diameter():
    """
    hardcode and return the diameter of the graph
    (run the function called “Network Diameter”) within Gephi
    """
    # you must replace this value with the graph diameter
    return 8

def avg_path_length():
    """
    hardcode and return the average path length
    (run the function called “Avg. Path Length”) within Gephi
    :return:
    """
    # you must replace this value with the avg path length
    return 4.417