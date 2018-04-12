### Author: <lefred$inuits,be> modified by: <curino@mit.edu>

global postgres_user
postgres_user = os.getenv('DSTAT_POSTGRES_USER') or os.getenv('USER')

global postgres_pwd
postgres_pwd = os.getenv('DSTAT_POSTGRES_PWD')

global postgres_host
postgres_host = os.getenv('DSTAT_POSTGRES_HOST')

global postgres_port
postgres_port = os.getenv('DSTAT_POSTGRES_PORT')

global postgres_db
postgres_db = os.getenv('DSTAT_POSTGRES_DB')


print "in postgres plugin"
class dstat_plugin(dstat):
    """
    Plugin for PostgreSQL ALL.
    """

    def __init__(self):
	print postgres_user,postgres_pwd,postgres_host,postgres_port,postgres_db
        self.name = 'postgres on ' + postgres_host +' ' + postgres_port
        self.nick = ()
	self.vars = ("confl_lock","xact_commit","xact_rollback","buffers_checkpoint","buffers_clean","buffers_backend","blks_read","blks_hit")
        
    def check(self): 
        global psycopg2
        import psycopg2
        try:
            self.db = psycopg2.connect(host=postgres_host,port=int(postgres_port), user=postgres_user, password=postgres_pwd, database=postgres_db)
        except:
            raise Exception, 'Cannot interface with PostgreSQL server'

    def extract(self):
        try:
            c = self.db.cursor()
            c.execute("""select confl_lock from pg_stat_database_conflicts;""")
            lines = c.fetchall()
            for line in lines:
		#print line[0]
		if 'confl_lock' in self.vars:
                    self.set2['confl_lock'] = float(line[0])

	    c.execute("""select xact_commit,xact_rollback from pg_stat_database;""")
            lines = c.fetchall()
            for line in lines:
		#print line[0],line[1]
                if 'xact_commit' in self.vars:
                    self.set2['xact_commit'] = float(line[0])
		if 'xact_rollback' in self.vars:
                    self.set2['xact_rollback'] = float(line[1])

	    c.execute("""select buffers_checkpoint,buffers_clean,buffers_backend from pg_stat_bgwriter;""")
            lines = c.fetchall()
            for line in lines:
                #print line[0],line[1],line[2]
                if 'buffers_checkpoint' in self.vars:
                    self.set2['buffers_checkpoint'] = float(line[0])
                if 'buffers_clean' in self.vars:
                    self.set2['buffers_clean'] = float(line[1])
                if 'buffers_backend' in self.vars:
                    self.set2['buffers_backend'] = float(line[2])

	    c.execute("""select blks_read,blks_hit from pg_stat_database;""")
            lines = c.fetchall()
            for line in lines:
                #print line[0],line[1]
                if 'blks_read' in self.vars:
                    self.set2['blks_read'] = float(line[0])
                if 'blks_hit' in self.vars:
                    self.set2['blks_hit'] = float(line[1])
		
	    c.execute("""select pg_stat_clear_snapshot();""") # clear statistics snapshot

            for name in self.vars:
                self.val[name] = self.set2[name] * 1.0 / elapsed

            if step == op.delay:
                self.set1.update(self.set2)

        except Exception, e:
            for name in self.vars:
                self.val[name] = -1

# vim:ts=4:sw=4:et


#if __name__=='__main__':
#  p = dstat_plugin()
#  p.check()
#  p.extract()

  
