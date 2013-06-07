#! /usr/bin/python

from types import *
import sys,re,operator
#import scipy.io

def parse_columns(monitor_file):
  fin = open(monitor_file, 'r')
  col1 = [re.sub(' .*','',x) for x in [x.strip('"') for x in fin.readline().strip().split(",")]]
  col2 = [x.strip('"').strip('\#') for x in fin.readline().strip().split(",")]
  if len(col1)!=len(col2):
    raise Exception("incorrect monitor header!")
  for i in range(0,len(col1)):
    col1[i] = re.sub('net\/eth([0-9])','net\\1',col1[i])
    col1[i] = re.sub('dsk\/sda','dsk',col1[i])
    col1[i] = re.sub('io\/sda','io',col1[i])
  fin.close()
  idx=0
  columns = {}
  sequence_no = {}
  while idx < len(col1):
    if columns.has_key(col1[idx]):
      columns[col1[idx]].append(col2[idx])
    else:
      columns[col1[idx]] = [col2[idx]]
      sequence_no[col1[idx]] = {}
    p_key = col1[idx]
    sequence_no[col1[idx]][col2[idx]] = idx+1
    idx+=1
    while idx<len(col1) and (col1[idx]==None or col1[idx]==''):
      columns[p_key].append(col2[idx])
      sequence_no[p_key][col2[idx]] = idx+1
      idx+=1
  return columns,sequence_no

def check_columns(columns):
  pass

def format_columns(columns,sequence_no):
  m_data = {}
  for key in columns:
    for v in columns[key]:
      if key=='postgres' or key=='system' or 'mysql' in key or len(columns[key])==1:
        m_data[v] = sequence_no[key][v]
      else:
        m_data[key+"_"+v] = sequence_no[key][v]
  #sorted_m_data = sorted(m_data.iteritems(), key=operator.itemgetter(1))
  return m_data

def format_interrupts(columns,sequence_no):
  interrupts = {}
  if 'interrupts' in columns:
    for v in columns['interrupts']:
      interrupts['i'+v] = sequence_no['interrupts'][v]
  return interrupts

def format_metadata(columns,sequence_no):
  metadata = {}
  metadata['num_cpu'] = 0
  metadata['num_net'] = 0
  metadata['interrupts'] = 'interrupts'
  for key in columns:
    if key.find('cpu') >= 0:
      metadata['num_cpu'] += 1
      for v in columns[key]:
        if metadata.has_key("cpu_"+v):
          metadata["cpu_"+v].append(sequence_no[key][v])
        else:
          metadata["cpu_"+v] = [sequence_no[key][v]]
    elif key.find('net') >=0:
      metadata['num_net'] += 1
      for v in columns[key]:
        if metadata.has_key("net_"+v):
          metadata["net_"+v].append(sequence_no[key][v])
        else:
          metadata["net_"+v] = [sequence_no[key][v]]
  return metadata

def format_header(columns):
  header = {'metadata':'metadata','columns':'columns'}
  header['dbms'] = 'unknown'
  for key in columns:
    if key.find('postgres') >= 0:
      header['dbms'] = 'psql'
    elif key.find('mysql') >= 0:
      header['dbms'] = 'mysql'
  return header

def write_struct(fout,name,values):
  fout.write(name+" = struct(")
  vlist = []
  for key in values:
    vlist.append("'"+key+"'")
    if values[key] in ['columns','metadata','interrupts']:
      vlist.append(values[key])
    elif type(values[key]) is IntType:
      vlist.append(str(values[key]))
    elif type(values[key]) is StringType:
      vlist.append("'"+values[key]+"'")
    elif type(values[key]) is ListType:
      vlist.append("["+' '.join([str(i) for i in values[key]])+"]")
    else:
      print "WWWW ",values[key]
      raise Exception("Bad type: "+str(type(values[key])))
  fout.write(",".join(vlist))
  fout.write(");\n")

if __name__=='__main__':
  if len(sys.argv) < 3:
    print "Usage: createMonitorMapping.py <monitor file> <output file>"
    exit(1)
  monitor_file = sys.argv[1]
  output_file = sys.argv[2]
  columns,sequence_no = parse_columns(monitor_file)
  check_columns(columns)

  formatted_columns = format_columns(columns,sequence_no)
  metadata = format_metadata(columns,sequence_no)
  header = format_header(columns)  

  print formatted_columns
  print metadata
  print header

  fout=open(output_file,'w')
  write_struct(fout,"columns",formatted_columns)
  write_struct(fout,"interrupts",format_interrupts(columns,sequence_no))
  write_struct(fout,"metadata",metadata)
  write_struct(fout,"header",header)
  fout.close()
