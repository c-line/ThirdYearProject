import org.json.JSONObject;

public class Testing	{
	public static void main(String[] args)	{
		
		String s = "{\"name\":\"Henry\"}";
		try	{
			JSONObject obj = new JSONObject(s);
		 	System.out.print(obj.get("name"));
		}catch(Exception e)	{
			e.printStackTrace();
		}
	}
}