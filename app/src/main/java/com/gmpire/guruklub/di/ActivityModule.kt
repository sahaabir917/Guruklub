package com.gmpire.guruklub.di

import com.gmpire.guruklub.view.activity.*
import com.gmpire.guruklub.view.activity.Registration.RegistrationActivity
import com.gmpire.guruklub.view.activity.batchQuestions.BatchQuestionActivity
import com.gmpire.guruklub.view.activity.categoryAndSubjectSelection.CategoryAndSubjectSelectionActivity
import com.gmpire.guruklub.view.activity.emailVerification.EmailVerificationActivity
import com.gmpire.guruklub.view.activity.forgetPassword.ForgetPasswordActivity
import com.gmpire.guruklub.view.activity.friendrequest.*
import com.gmpire.guruklub.view.activity.gameActivity.GameActivity
import com.gmpire.guruklub.view.activity.gameChallenge.GameChallengeActivity
import com.gmpire.guruklub.view.activity.gameHelp.ContentActivity
import com.gmpire.guruklub.view.activity.gameLevelQuestion.GameLevelQuestionActivity
import com.gmpire.guruklub.view.activity.gameResultActivity.GameResultActivity
import com.gmpire.guruklub.view.activity.gameheart.HeartAddActivity
import com.gmpire.guruklub.view.activity.gameheart.HeartChallengeSuccessDialogFragment
import com.gmpire.guruklub.view.activity.gamelevel.FriendRequestActivity
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelActivity
import com.gmpire.guruklub.view.activity.gamesubscribe.SubscribeDialog
import com.gmpire.guruklub.view.activity.helpAndSupport.HelpAndSupportActivity
import com.gmpire.guruklub.view.activity.howToGetCoin.HowToGetCoinActivity
import com.gmpire.guruklub.view.activity.infoCenter.InfoCenterActivity
import com.gmpire.guruklub.view.activity.leaderDetailsActivity.LeaderDetailsActivity
import com.gmpire.guruklub.view.activity.library.*
import com.gmpire.guruklub.view.activity.login.*
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.modelTestActivity.ModelTestActivity
import com.gmpire.guruklub.view.activity.newsDetails.NewsDetailsActivity
import com.gmpire.guruklub.view.activity.notification.NotificationActivity
import com.gmpire.guruklub.view.activity.notificationDetails.NotificationDetailsActivity
import com.gmpire.guruklub.view.activity.phoneVerification.PhoneVerificationActivity
import com.gmpire.guruklub.view.activity.playwithfriends.PlayWithFriendActivity
import com.gmpire.guruklub.view.activity.playwithfriends.PlaywithFriendWinnerActivity
import com.gmpire.guruklub.view.activity.profile.*
import com.gmpire.guruklub.view.activity.profileSetup.ProfileSetupActivity
import com.gmpire.guruklub.view.activity.question.QuestionAddActivity
import com.gmpire.guruklub.view.activity.question.QuestionAddNextActivity
import com.gmpire.guruklub.view.activity.questionSearch.QuestionSearchActivity
import com.gmpire.guruklub.view.activity.questionShare.QuestionShareActivity
import com.gmpire.guruklub.view.activity.resetPassword.ResetPasswordActivity
import com.gmpire.guruklub.view.activity.settings.SettingsActivity
import com.gmpire.guruklub.view.activity.subjectBasedPerformace.SubjectBasedPerformanceActivity
import com.gmpire.guruklub.view.activity.termsAndCondition.TermsConditionActivity
import com.gmpire.guruklub.view.activity.viewSolutions.ViewSolutionsActivity
import com.gmpire.guruklub.view.activity.youtube.YoutubeActivity
import com.gmpire.guruklub.view.dialog.InviteFriendPromoCodeDialog
import com.gmpire.guruklub.view.dialog.NewsDetailsGestureDialog
import com.gmpire.guruklub.view.dialog.UpdateProfileDialog
import com.gmpire.guruklub.view.dialog.gameLevel.*
import com.gmpire.guruklub.view.fragment.QuestionSearchFragment
import com.gmpire.guruklub.view.fragment.favourite.FavouriteFragment
import com.gmpire.guruklub.view.fragment.game.GameFragment

import com.gmpire.guruklub.view.dialog.gameLevel.GameChallengeDialog
import com.gmpire.guruklub.view.dialog.notice.NoticeDetailsActivity
import com.gmpire.guruklub.view.fragment.dashboard.DashboardFragment

import com.gmpire.guruklub.view.fragment.helpAndSupport.HelpAndSupportFragment
import com.gmpire.guruklub.view.fragment.home.HomeFragment
import com.gmpire.guruklub.view.fragment.infoCentre.InfoCentreFragment

import com.gmpire.guruklub.view.fragment.library.LibraryFragment
import com.gmpire.guruklub.view.fragment.library.LibraryFragmentNew
import com.gmpire.guruklub.view.fragment.news.NewsFragment
import com.gmpire.guruklub.view.fragment.notification.NotificationFragment
import com.gmpire.guruklub.view.fragment.populer.PopularFragment
import com.gmpire.guruklub.view.fragment.previousQuestionsBatch.PreviousQuestionBatchFragment
import com.gmpire.guruklub.view.fragment.profile.ProfileMainFragment
import com.gmpire.guruklub.view.fragment.settings.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    internal abstract fun contributeSplashActivity(): SplashActivity

    @ContributesAndroidInjector
    internal abstract fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector
    internal abstract fun QuestionShareActivity(): QuestionShareActivity

    @ContributesAndroidInjector
    internal abstract fun LeaderBoardActivity(): LeaderBoardActivity

    @ContributesAndroidInjector
    internal abstract fun ErrorActivity(): ErrorActivity

    @ContributesAndroidInjector
    internal abstract fun FavouriteActivity(): FavouriteActivity

    @ContributesAndroidInjector
    internal abstract fun contributeInfoCenterActivity(): InfoCenterActivity

    @ContributesAndroidInjector
    internal abstract fun contributeHelpAndSupportActivity(): HelpAndSupportActivity

    @ContributesAndroidInjector
    internal abstract fun RelatedVideoActivity(): RelatedVideoActivity

    @ContributesAndroidInjector
    internal abstract fun contributeSubjectBasedPerformanceActivity(): SubjectBasedPerformanceActivity

    @ContributesAndroidInjector
    internal abstract fun contributeViewSolutionsActivity(): ViewSolutionsActivity

    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun ResetPasswordActivity(): ResetPasswordActivity

    @ContributesAndroidInjector
    internal abstract fun PhoneVerificationActivity(): PhoneVerificationActivity

    @ContributesAndroidInjector
    internal abstract fun TermsConditionActivity(): TermsConditionActivity

    @ContributesAndroidInjector
    internal abstract fun GameHelpActivity(): ContentActivity

    @ContributesAndroidInjector
    internal abstract fun RegistrationActivity(): RegistrationActivity

    @ContributesAndroidInjector
    internal abstract fun EmailVerificationActivity(): EmailVerificationActivity

    @ContributesAndroidInjector
    internal abstract fun ProfileSetupActivity(): ProfileSetupActivity

    @ContributesAndroidInjector
    internal abstract fun LibraryActivity(): LibraryActivity

    @ContributesAndroidInjector
    internal abstract fun HowToGetCoinActivity(): HowToGetCoinActivity

    @ContributesAndroidInjector
    internal abstract fun GameActivity(): GameActivity

    @ContributesAndroidInjector
    internal abstract fun GameResultActivity(): GameResultActivity

    @ContributesAndroidInjector
    internal abstract fun ProfileActivity(): ProfileActivity

    @ContributesAndroidInjector
    internal abstract fun QuestionDetailsActivity(): QuestionDetailsActivity

    @ContributesAndroidInjector
    internal abstract fun QuestionAddActivity(): QuestionAddActivity

    @ContributesAndroidInjector
    internal abstract fun NotificationActivity(): NotificationActivity

    @ContributesAndroidInjector
    internal abstract fun QuestionAddNextActivity(): QuestionAddNextActivity


    @ContributesAndroidInjector
    internal abstract fun LeaderDetailsActivity(): LeaderDetailsActivity

    @ContributesAndroidInjector
    internal abstract fun NewsDetailsActivity(): NewsDetailsActivity

    @ContributesAndroidInjector
    internal abstract fun SuccessOrFailActivity(): SuccessOrFailActivity

    @ContributesAndroidInjector
    internal abstract fun PerformanceHistoryActivity(): PerformanceHistoryActivity

    @ContributesAndroidInjector
    internal abstract fun contributeHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    internal abstract fun contributeLibraryFragment(): LibraryFragment

    @ContributesAndroidInjector
    internal abstract fun contributeGameFragment(): GameFragment

    @ContributesAndroidInjector
    internal abstract fun StudyFragment(): StudyFragment

    @ContributesAndroidInjector
    internal abstract fun QuestionsFragment(): QuestionsFragment

    @ContributesAndroidInjector
    internal abstract fun ProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    internal abstract fun LeaderBoardFragment(): LeaderBoardFragment

    @ContributesAndroidInjector
    internal abstract fun ErrorFragment(): ErrorFragment

    @ContributesAndroidInjector
    internal abstract fun FavaouriteFragment(): FavouriteFragment

    @ContributesAndroidInjector
    internal abstract fun NewsFragment(): NewsFragment

    @ContributesAndroidInjector
    internal abstract fun PopularFragment(): PopularFragment

    @ContributesAndroidInjector
    internal abstract fun PreviousQuestionBatchFragment(): PreviousQuestionBatchFragment

    @ContributesAndroidInjector
    internal abstract fun BatchQuestionActivity(): BatchQuestionActivity

    @ContributesAndroidInjector
    internal abstract fun contributeNotificationFragment(): NotificationFragment

    @ContributesAndroidInjector
    internal abstract fun contributeForgetPasswordActivity(): ForgetPasswordActivity

    @ContributesAndroidInjector
    internal abstract fun contributeModelTestActivity(): ModelTestActivity

    @ContributesAndroidInjector
    internal abstract fun contributeNotificationDetailsActivity(): NotificationDetailsActivity

    @ContributesAndroidInjector
    internal abstract fun contributeSettingsActivity(): SettingsActivity

    @ContributesAndroidInjector
    internal abstract fun contributeCategoryAndSubjectSelectionActivity(): CategoryAndSubjectSelectionActivity

    @ContributesAndroidInjector
    internal abstract fun contributeQuestionSearchActivity(): QuestionSearchActivity

    @ContributesAndroidInjector
    internal abstract fun contributeQuestionSearchFragment(): QuestionSearchFragment

    @ContributesAndroidInjector
    internal abstract fun contributeProfileMainFragment(): ProfileMainFragment

    @ContributesAndroidInjector
    internal abstract fun contributeInfoCentreFragment(): InfoCentreFragment

    @ContributesAndroidInjector
    internal abstract fun contributeHelpAndSupportFragment(): HelpAndSupportFragment

    @ContributesAndroidInjector
    internal abstract fun contributeSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    internal abstract fun contributeLibraryFragmentNew(): LibraryFragmentNew

    @ContributesAndroidInjector
    internal abstract fun contributeSingleVideoActivity(): SingleVideoActivity

    @ContributesAndroidInjector
    internal abstract fun contributeGameLevelActivity(): GameLevelActivity

    @ContributesAndroidInjector
    internal abstract fun contributeHeartAddActivity(): HeartAddActivity

    @ContributesAndroidInjector
    internal abstract fun contributeLevelUpActivity(): GameLevelUpDialog

    @ContributesAndroidInjector
    internal abstract fun contributeGameSettingActivity(): GameSettingDialog

    @ContributesAndroidInjector
    internal abstract fun contributeSubscribeActivity(): SubscribeDialog

    @ContributesAndroidInjector
    internal abstract fun contributeGameLevelQuestionActivity(): GameLevelQuestionActivity

    @ContributesAndroidInjector
    internal abstract fun contributeGameRuleDialogFragment(): GameLevelInfoDialog

    @ContributesAndroidInjector
    internal abstract fun contributeGameLevelOverDialogFragment(): GameLevelOverDialog

    @ContributesAndroidInjector
    internal abstract fun contributeGameChallengeFragment(): GameChallengeDialog

    @ContributesAndroidInjector
    internal abstract fun contributeGameChallengeActivity(): GameChallengeActivity

    @ContributesAndroidInjector
    internal abstract fun contributeHeartChallengeSuccessDialogFragment(): HeartChallengeSuccessDialogFragment

    @ContributesAndroidInjector
    internal abstract fun contributeInviteFriendPromoCodeDialog(): InviteFriendPromoCodeDialog

    @ContributesAndroidInjector
    internal abstract fun contributeGameChallengeFailedDialog(): GameChallengeFailedDialog

    @ContributesAndroidInjector
    internal abstract fun contributeAllVideosActivity(): AllVideosActivity

    @ContributesAndroidInjector
    internal abstract fun contributeAddVideoActivity(): AddVideoActivity

    @ContributesAndroidInjector
    internal abstract fun contributeAddVideoNextActivity(): AddVideoNextActivity

    @ContributesAndroidInjector
    internal abstract fun contributeAddVideoSuccessFull(): AddVideoSuccessFull

    @ContributesAndroidInjector
    internal abstract fun contributeYoutbeActivity(): YoutubeActivity

    @ContributesAndroidInjector
    internal abstract fun contributeUpdateProfileDialog(): UpdateProfileDialog

    @ContributesAndroidInjector
    internal abstract fun contributeAllLibrarySubject(): AllLibrarySubject

    @ContributesAndroidInjector
    internal abstract fun contributeNewsDetailsGestureDialog(): NewsDetailsGestureDialog

    @ContributesAndroidInjector
    internal abstract fun contributeDashBoardFragment(): DashboardFragment

    @ContributesAndroidInjector
    internal abstract fun contributePlayWithFriendActivity(): PlayWithFriendActivity

    @ContributesAndroidInjector
    internal abstract fun contributePlayWithFriendWinnerActivity(): PlaywithFriendWinnerActivity

    @ContributesAndroidInjector
    internal abstract fun contributeFriendRequestFragment(): FriendRequestFragment

    @ContributesAndroidInjector
    internal abstract fun contributeAddFriendFragment(): AddFriendFragment

    @ContributesAndroidInjector
    internal abstract fun contributeMyFriendFragment(): MyFriendFragment

    @ContributesAndroidInjector
    internal abstract fun contributeRequestFriendFragment(): RequestFriendFragment

    @ContributesAndroidInjector
    internal abstract fun contributeRecivedPendingFriendRequest(): RecivedPendingFriendRequest

    @ContributesAndroidInjector
    internal abstract fun contributeFriendRequestActivity(): FriendRequestActivity

    @ContributesAndroidInjector
    internal abstract fun contributeHeartChooseDialog(): HeartChooseDialog

    @ContributesAndroidInjector
    internal abstract fun contributeNoticeDetailsActivity(): NoticeDetailsActivity

}