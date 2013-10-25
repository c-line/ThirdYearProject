require 'mysql2' #uses a "gem" same as import java.util for example

def connect_to_sql

	begin #java equivelance is try (and catch)
		con = Mysql2::Client.new(host: 'localhost', username: 'root', password: '')

		#Try and find the database, if it doesn't exist, create it
		found = false
		databases = con.query("SHOW DATABASES")
		databases.each do |database|
			if database['Database']=="thirdyearproject"
				#if database exists, use it
				#Don't need to reapply the schema because want to keep all the data 
				#in there before starting
				con.query("USE thirdyearproject")
				found = true
				puts "Are you sure you want to empty this database? (y/n)"
				STDOUT.flush
				input = gets.chomp
				if input=="y"
					puts "Are you super super super DUPER sure? THIS WILL ERASE ALL DATA COLLECTED! (yes i am sure/n)"
					STDOUT.flush
					input2 = gets.chomp
					if input2=="yes i am sure"
						con.query("drop database if exists thirdyearproject")
						puts "Database has been deleted!!!!!!"
					end
				end
			end
		end
		#if not found, create it 
		if found == false
			puts "Error: Not found the database!!!"
		end
		
		rescue Exception => e #catch
			raise e

		ensure #will run regardless of failures
			con.close if con
		end
end

def input_test

	puts "yes or no"
	STDOUT.flush #this flushes ruby's buffer
	input = gets.chomp
	puts "answer is " + input

end

connect_to_sql