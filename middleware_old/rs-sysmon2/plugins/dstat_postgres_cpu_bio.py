### Authority: Barzan Mozafari <barzan@csail.mit.edu>

class dstat_plugin(dstat):
    """
    The CPU usage and the physical IO of postgres process.

    Displays the total CPU usage (%) and the block read and writes of the postgres process in bytes (read_bytes and write_bytes) during the monitored interval
    """
    def __init__(self):
        self.name = 'postgres usage'
        self.vars = ('postgres_cpu','postgres_children_cpu','postgres_bytes_read','postgres_bytes_written')
        self.type = 'd'
        self.width = 20
        self.scale = 0
	self.postgres_pid = -1
	print "dstat plugin init"
        for pid in proc_pidlist():
            try:
                ### Using dopen() will cause too many open files
                l = proc_splitline('/proc/%s/stat' % pid)
            except IOError:
                continue

            if len(l) < 17: continue

            name = l[1][1:-1]
            try:
            ## Using dopen() will cause too many open files
                pname = getnamebypid(pid, name)
                if str(pname).strip() == 'postgres_alekh': # this is the line we need to change to make this work for any given process
                    self.postgres_pid = pid
		    break			
            except IOError:
               continue
        #print self.postgres_pid
        self.val = {}
        self.keyset1 = {}; self.keyset2 = {}	
        self.keyset1['postgres_cpu'] = 0.0
        self.keyset1['postgres_children_cpu'] = 0.0
        self.keyset1['postgres_bytes_read'] = 0
        self.keyset1['postgres_bytes_written'] = 0
	
    def extract(self):
        try:
            ### Using dopen() will cause too many open files
	    l = proc_splitline('/proc/%s/stat' % self.postgres_pid)
        except IOError:
            sys.stderr.write('dstat plugin couldn not open /proc/%s/stat' % self.postgres_pid)
        
        if len(l) < 17: 
            sys.stderr.write('dstat plugin bad format in /proc/%s/stat' % self.postgres_pid)

        self.keyset2['postgres_cpu'] = long(l[13]) + long(l[14])
        self.keyset2['postgres_children_cpu'] = long(l[15]) + long(l[16])
        #print self.val.keys()
        #print self.val.values()
        self.val['postgres_cpu'] = (self.keyset2['postgres_cpu'] - self.keyset1['postgres_cpu']) * 1.0 / elapsed / cpunr
        self.val['postgres_children_cpu'] = (self.keyset2['postgres_children_cpu'] - self.keyset1['postgres_children_cpu']) * 1.0 / hz / elapsed / cpunr
        #print elapsed, cpunr, hz, self.keyset2['postgres_cpu'], self.keyset1['postgres_cpu']
        self.keyset2['postgres_bytes_read'] = -1
        self.keyset2['postgres_bytes_written'] = -1
        try:
            ### Extract counters
            for l in proc_splitlines('/proc/%s/io' % self.postgres_pid):
                if len(l) != 2: continue
                if l[0].startswith('read_bytes'):
                    self.keyset2['postgres_bytes_read'] = int(l[1])
                if l[0].startswith('write_bytes'):
                    self.keyset2['postgres_bytes_written'] = int(l[1])
        except IOError:
           sys.stderr.write('dstat plugin IO error reading /proc/%s/io' % self.postgres_pid)
        if self.keyset2['postgres_bytes_read']<0 or self.keyset2['postgres_bytes_written']<0:
           sys.stderr.write('Some of the variables are negative in dstat plugin!\n')

        self.val['postgres_bytes_read'] = (self.keyset2['postgres_bytes_read'] - self.keyset1['postgres_bytes_read']) * 1.0 / elapsed
        self.val['postgres_bytes_written'] = (self.keyset2['postgres_bytes_written'] - self.keyset1['postgres_bytes_written']) * 1.0 / elapsed

        #if step == op.delay:   #BM: I don't know what this check means at all?!
        #     self.pidset1.update(self.pidset2)
        if step == op.delay:
            self.keyset1.update(self.keyset2)

#    def showcsv(self):
#        return '% / %d%%' % (self.val['name'], self.val['max'])

# vim:ts=4:sw=4:et
