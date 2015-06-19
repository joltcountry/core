using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class PlayerController : MonoBehaviour {

	private Rigidbody rb;
	public float speed;
	private int score;
	public Text scoreText;
	public Text winText;

	void Start() {
		rb = GetComponent<Rigidbody> ();
		score = 0;
		SetScore ();
		winText.text = "";
	}

	void Update() {
		if (Input.GetKey (KeyCode.Escape)) {
			Application.Quit ();
		}
	}
	
	void OnTriggerEnter(Collider other) {
		if (other.gameObject.CompareTag ("pickup")) {
			other.gameObject.SetActive (false);
			score++;
			SetScore ();
		}
	}

	void FixedUpdate() {
		float moveHorizontal = Input.GetAxis ("Horizontal") * speed;
		float moveVertical = Input.GetAxis ("Vertical") * speed;
		rb.AddForce(new Vector3(moveHorizontal, 0, moveVertical));
	}

	void SetScore() {
		scoreText.text = "Your score, supposedly, is: " + score.ToString ();
		if (score >= 11) {
			winText.text = "YOU WIN!!! WHOOPIE!!";
		}
	}

}
