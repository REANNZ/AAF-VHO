package aaf.vhr.crypto;

// Copyright (c) 2006 Damien Miller <djm@mindrot.org>
//
// Permission to use, copy, modify, and distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

import spock.lang.*

/**
 * JUnit unit tests for BCrypt routines
 * @author Damien Miller
 * @version 0.2
 */

class BCryptSpec extends Specification {
	@Shared def test_vectors = [
			// [0] is the plaintext information, [1] is the salt, and [2] is the salted hash
			[ "", "\$2a\$06\$DCq7YPn5Rq63x1Lad4cll.", "\$2a\$06\$DCq7YPn5Rq63x1Lad4cll.TV4S6ytwfsfvkgY8jIucDrjc8deX1s." ],
			[ "", "\$2a\$08\$HqWuK6/Ng6sg9gQzbLrgb.", "\$2a\$08\$HqWuK6/Ng6sg9gQzbLrgb.Tl.ZHfXLhvt/SgVyWhQqgqcZ7ZuUtye" ],
			[ "", "\$2a\$10\$k1wbIrmNyFAPwPVPSVa/ze", "\$2a\$10\$k1wbIrmNyFAPwPVPSVa/zecw2BCEnBwVS2GbrmgzxFUOqW9dk4TCW" ],
			[ "", "\$2a\$12\$k42ZFHFWqBp3vWli.nIn8u", "\$2a\$12\$k42ZFHFWqBp3vWli.nIn8uYyIkbvYRvodzbfbK18SSsY.CsIQPlxO" ],
			[ "a", "\$2a\$06\$m0CrhHm10qJ3lXRY.5zDGO", "\$2a\$06\$m0CrhHm10qJ3lXRY.5zDGO3rS2KdeeWLuGmsfGlMfOxih58VYVfxe" ],
			[ "a",  "\$2a\$08\$cfcvVd2aQ8CMvoMpP2EBfe", "\$2a\$08\$cfcvVd2aQ8CMvoMpP2EBfeodLEkkFJ9umNEfPD18.hUF62qqlC/V." ],
			[ "a", "\$2a\$10\$k87L/MF28Q673VKh8/cPi.", "\$2a\$10\$k87L/MF28Q673VKh8/cPi.SUl7MU/rWuSiIDDFayrKk/1tBsSQu4u" ],
			[ "a", "\$2a\$12\$8NJH3LsPrANStV6XtBakCe", "\$2a\$12\$8NJH3LsPrANStV6XtBakCez0cKHXVxmvxIlcz785vxAIZrihHZpeS" ],
			[ "abc", "\$2a\$06\$If6bvum7DFjUnE9p2uDeDu", "\$2a\$06\$If6bvum7DFjUnE9p2uDeDu0YHzrHM6tf.iqN8.yx.jNN1ILEf7h0i" ],
			[ "abc", "\$2a\$08\$Ro0CUfOqk6cXEKf3dyaM7O", "\$2a\$08\$Ro0CUfOqk6cXEKf3dyaM7OhSCvnwM9s4wIX9JeLapehKK5YdLxKcm" ],
			[ "abc", "\$2a\$10\$WvvTPHKwdBJ3uk0Z37EMR.", "\$2a\$10\$WvvTPHKwdBJ3uk0Z37EMR.hLA2W6N9AEBhEgrAOljy2Ae5MtaSIUi" ],
			[ "abc", "\$2a\$12\$EXRkfkdmXn2gzds2SSitu.", "\$2a\$12\$EXRkfkdmXn2gzds2SSitu.MW9.gAVqa9eLS1//RYtYCmB1eLHg.9q" ],
			[ "abcdefghijklmnopqrstuvwxyz", "\$2a\$06\$.rCVZVOThsIa97pEDOxvGu", "\$2a\$06\$.rCVZVOThsIa97pEDOxvGuRRgzG64bvtJ0938xuqzv18d3ZpQhstC" ],
			[ "abcdefghijklmnopqrstuvwxyz", "\$2a\$08\$aTsUwsyowQuzRrDqFflhge", "\$2a\$08\$aTsUwsyowQuzRrDqFflhgekJ8d9/7Z3GV3UcgvzQW3J5zMyrTvlz." ],
			[ "abcdefghijklmnopqrstuvwxyz", "\$2a\$10\$fVH8e28OQRj9tqiDXs1e1u", "\$2a\$10\$fVH8e28OQRj9tqiDXs1e1uxpsjN0c7II7YPKXua2NAKYvM6iQk7dq" ],
			[ "abcdefghijklmnopqrstuvwxyz", "\$2a\$12\$D4G5f18o7aMMfwasBL7Gpu", "\$2a\$12\$D4G5f18o7aMMfwasBL7GpuQWuP3pkrZrOAnqP.bmezbMng.QwJ/pG" ],
			[ "~!@#\$%^&*()      ~!@#\$%^&*()PNBFRD", "\$2a\$06\$fPIsBO8qRqkjj273rfaOI.", "\$2a\$06\$fPIsBO8qRqkjj273rfaOI.HtSV9jLDpTbZn782DC6/t7qT67P6FfO" ],
			[ "~!@#\$%^&*()      ~!@#\$%^&*()PNBFRD", "\$2a\$08\$Eq2r4G/76Wv39MzSX262hu", "\$2a\$08\$Eq2r4G/76Wv39MzSX262huzPz612MZiYHVUJe/OcOql2jo4.9UxTW" ],
			[ "~!@#\$%^&*()      ~!@#\$%^&*()PNBFRD", "\$2a\$10\$LgfYWkbzEvQ4JakH7rOvHe", "\$2a\$10\$LgfYWkbzEvQ4JakH7rOvHe0y8pHKF9OaFgwUZ2q7W2FFZmZzJYlfS" ],
			[ "~!@#\$%^&*()      ~!@#\$%^&*()PNBFRD", "\$2a\$12\$WApznUOJfkEGSmYRfnkrPO", "\$2a\$12\$WApznUOJfkEGSmYRfnkrPOr466oFDCaj4b6HY3EXGvfxm43seyhgC" ],
	];

	/**
	 * Entry point for unit tests
	 * @param args unused
  *	public static void main(String[] args) {
 * 		junit.textui.TestRunner.run(TestBCrypt.class);
 * 	} 
  */

	/**
	 * Test method for 'BCrypt.hashpw(String, String)'
	 */
	 @Unroll
	def 'testHashpw'() {
		when:
		def plain = test_vectors[i][0]
		def salt = test_vectors[i][1]
		def expected = test_vectors[i][2]
		def hashed = BCrypt.hashpw(plain, salt)

		then:
		hashed == expected

		where:
		i << (0..test_vectors.size() - 1)
	}

	/**
	 * Test method for 'BCrypt.gensalt(int)'
	 */
	def 'testGensaltInt'() {
        when:
		def plain = test_vectors[j][0]
        def salt = BCrypt.gensalt(i)
        def hashed1 = BCrypt.hashpw(plain, salt)
        def hashed2 = BCrypt.hashpw(plain, hashed1)

		then:
        hashed1 == hashed2

        where:
		i  | j  || _
		4  | 0  || _
		4  | 4  || _
		4  | 8  || _
		4  | 12 || _
		4  | 16 || _
		5  | 0  || _
		5  | 4  || _
		5  | 8  || _
		5  | 12 || _
		5  | 16 || _
		6  | 0  || _
		6  | 4  || _
		6  | 8  || _
		6  | 12 || _
		6  | 16 || _
		7  | 0  || _
		7  | 4  || _
		7  | 8  || _
		7  | 12 || _
		7  | 16 || _
		8  | 0  || _
		8  | 4  || _
		8  | 8  || _
		8  | 12 || _
		8  | 16 || _
		9  | 0  || _
		9  | 4  || _
		9  | 8  || _
		9  | 12 || _
		9  | 16 || _
		10 | 0  || _
		10 | 4  || _
		10 | 8  || _
		10 | 12 || _
		10 | 16 || _
		11 | 0  || _
		11 | 4  || _
		11 | 8  || _
		11 | 12 || _
		11 | 16 || _
		12 | 0  || _
		12 | 4  || _
		12 | 8  || _
		12 | 12 || _
		12 | 16 || _
	}

	/**
	 * Test method for 'BCrypt.gensalt()'
	 */
	 @Unroll
	def 'testGensalt'() {
		when:
		def plain = test_vectors[i][0]
		def salt = BCrypt.gensalt()
		def hashed1 = BCrypt.hashpw(plain, salt)
		def hashed2 = BCrypt.hashpw(plain, hashed1)

		then:
		hashed1 == hashed2

		where:
		i << (0..test_vectors.size() - 1).step(4)
	}

	/**
	 * Test method for 'BCrypt.checkpw(String, String)'
	 * expecting success
	 */
	 @Unroll
	def 'testCheckpw_success'() {
		when:
		def plain = test_vectors[i][0]
		def expected = test_vectors[i][2]

		then:
		BCrypt.checkpw(plain, expected)

		where:
		i << (0..test_vectors.size() - 1)
	}

	/**
	 * Test method for 'BCrypt.checkpw(String, String)'
	 * expecting failure
	 */
	 @Unroll
	def 'testCheckpw_failure'() {
		when:
		def plain = test_vectors[i][0]
		def broken_index = (i + 4) % test_vectors.size()
		def expected = test_vectors[broken_index][2]

		then:
		!BCrypt.checkpw(plain, expected)

		where:
		i << (0..test_vectors.size() - 1)
	}

	/**
	 * Test for correct hashing of non-US-ASCII passwords
	 */
	def 'testInternationalChars'() {
		when:
		def pw1 = "ππππππππ";
		def pw2 = "????????";
		def h1 = BCrypt.hashpw(pw1, BCrypt.gensalt());
		def h2 = BCrypt.hashpw(pw2, BCrypt.gensalt());

		then:
		!BCrypt.checkpw(pw2, h1)
		!BCrypt.checkpw(pw1, h2)
	}

}
