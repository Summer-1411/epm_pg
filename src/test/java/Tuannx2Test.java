import java.util.ArrayList;
import java.util.List;

import com.fis.epm.models.TokenModel;
import com.fis.pg.common.utils.Tools;

public class Tuannx2Test {
	public static void main(String[] args) {
		List<TokenModel> bank = new ArrayList<TokenModel>();
		bank.add(new TokenModel());
		System.out.println(Tools.convertModelSerialToString(bank));
	}
}
