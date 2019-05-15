package kozuma.shun.techacademy.jp

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ListView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.activity_question_send.*

import com.google.firebase.database.DataSnapshot
import kotlin.collections.HashMap




class QuestionDetailActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference

    //お気に入り判断
    private var likestar: Boolean? = null
    //お気に入りデータ
    private lateinit var mDataBaseReference: DatabaseReference

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {



            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    private val post = object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser!!.uid

            val key = dataSnapshot.child(FavoritesPATH).child(user).child(mQuestion.questionUid).value
            println("key  "+key + " mQuestion.questionUid" + mQuestion.questionUid  )

            if(key != null){
                textViewLike.text = "⭐"
                likestar = true
            }else{
                textViewLike.text = "☆"
                likestar = false
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        mFavoriteRef = FirebaseDatabase.getInstance().reference


        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()


        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef =
            dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid)
                .child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)


        //お気に入りのID取得
        mFavoriteRef.addValueEventListener(post)


        textViewLike.setOnClickListener(this)

    }


    override fun onClick(v: View) {
        val user = FirebaseAuth.getInstance().currentUser!!.uid

        if (v === textViewLike) {
            println("クリック" + likestar)
            if (likestar == false){
                textViewLike.text = "⭐"
                likestar = true
                println("-----------"+likestar)

                val favoriteRef = mDataBaseReference.child(FavoritesPATH).child(user).child(mQuestion.questionUid)
                val data = HashMap<String, String>()

                data["genre"] = mQuestion.genre.toString()
                favoriteRef.setValue(data)

            }else if(likestar == true){

                textViewLike.text = "☆"
                likestar = false
                println("-----------"+likestar)

                val favoriteRef = mDataBaseReference.child(FavoritesPATH).child(user).child(mQuestion.questionUid)

                //println(favoriteRef)
                favoriteRef.removeValue()

            }
        }
    }


}



